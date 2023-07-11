package com.github.penndev.service

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class TunService : BaseService() {

    private var tunFd: FileDescriptor? = null

    private lateinit var serviceSock: DatagramChannel

    private fun setService() {
        serviceSock = DatagramChannel.open()
        if(!protect(serviceSock.socket())) {
            throw Exception("启动代理设备失败")
        }
        serviceSock.connect(InetSocketAddress(serviceIp, servicePort))

        tun = Builder()
            .setMtu(1400)
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)
            .addAddress("10.0.0.2", 32)
            .addDisallowedApplication(packageName)
            .establish() ?: throw Exception("启动隧道失败")
        tunFd = tun?.fileDescriptor!!
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
                    val tunWrite = FileOutputStream(tunFd)
                    serviceSock.configureBlocking(false)
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
                Log.e("penndev", "服务引起异常", e)
            }
        }

    }

}

