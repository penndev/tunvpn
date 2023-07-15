package com.github.penndev.service

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class TunService : BaseService() {

    private var tunFd: FileDescriptor? = null

    private lateinit var serviceSock: DatagramChannel

    private fun setDevice(mtu:Int, ip:String, dns: String) {
        tun = Builder()
            .setMtu(mtu)
            .addAddress(ip, 32)
            .addDnsServer(dns)
            .addRoute("0.0.0.0", 0)
            .addDisallowedApplication(packageName)
            .establish() ?: throw Exception("启动隧道失败")
        tunFd = tun?.fileDescriptor!!
    }

    private suspend fun setService() {
        serviceSock = DatagramChannel.open()
        if(!protect(serviceSock.socket())) {
            throw Exception("启动代理设备失败")
        }
        serviceSock.connect(InetSocketAddress(serviceIp, servicePort))
        serviceSock.configureBlocking(false)

        val auth = JSONObject()
        auth.put("username", serviceUserName)
        auth.put("password", servicePassword)
        var authByte = byteArrayOf(0) + auth.toString().toByteArray()
        var authBuffer = ByteBuffer.wrap( authByte )

        serviceSock.write(authBuffer)

        serviceSock.write(authBuffer)
        repeat(3) { iteration ->
            serviceSock.write(authBuffer)
            delay(1000) // 阻塞一秒
            val packet = ByteBuffer.allocate(32767)
            val readLen = serviceSock.read(packet)
            if (readLen > 0 && packet[0].toInt() == 0 ){
                var res = JSONObject( String(packet.array(), 1, readLen-1))
                if ( res.getInt("code") != 1) {
                    throw Exception("用户认证失败")
                }
                return setDevice(res.getInt("mtu"), res.getString("ip"),res.getString("dns"))
            }
        }
        throw Exception("超时")
    }

    override fun setupVpnServe() {
        Log.i("penndev", "setupVpnServe tun")
        setupNotifyForeground() // 初始化启动通知
        job = GlobalScope.launch {
            try {
                setService()
                updateNotification("发送通知完成")

                val readSock = launch {
                    val packet = ByteBuffer.allocate(32767)
                    val tunWrite = FileOutputStream(tunFd!!)
                    while (true){
                        val readLen = serviceSock.read(packet)
                        if (readLen > 0){
                            if (packet[0].toInt() != 0) {
                                tunWrite.write(packet.array(),0,readLen)
                            }
                            packet.clear()
                        }else {
                            delay(100)
                        }
                    }
                }

                var readTun = launch {
                    val packet = ByteBuffer.allocate(32767)
                    val tunReader = FileInputStream(tunFd)
                    while (true){
                        val readLen = tunReader.read(packet.array())
                        if (readLen > 0){
                            packet.limit(readLen)
                            serviceSock.write(packet)
                            packet.clear()
                        }else {
                            delay(100)
                        }
                    }
                }

                readSock.join()
                readTun.join()
            } catch (e: Exception) { //处理抛出异常问题
                updateNotification(e.message.toString())
                Log.e("penndev", "服务引起异常", e)
            }
        }

    }

}

