package com.github.penndev

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel


class TunService : VpnService() {
    private var status: Boolean = false
    // tun 设备
    private var localTunnel: ParcelFileDescriptor? = null

    // 工作进程
    private var job: Job? = null

    // 远程服务器IP地址
    private var serviceIp: String? = null

    // 远程服务器端口
    private var servicePort: Int = 0

    //private var pendingIntent: PendingIntent? = null
    //override fun onCreate() {
    //}

    /**
     * 启动命令
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("penndev", "Android TunService onStartCommand $status")
        // 处理启动的参数
        try {
            setupCommand(intent)
        }catch (e: Exception){
            Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
            return START_NOT_STICKY
        }

        // 转换为前端服务
        setupForeground()
        // 启动/dev/tun 设备
        setupBuilder()

        job = GlobalScope.launch {
            Log.i("penndev", "job:  start")

            // 创建本地tun设备
            val tunFd = localTunnel?.fileDescriptor
            val tunRead = FileInputStream(tunFd)
            val tunWrite = FileOutputStream(tunFd)

            // 创建 udp socket
            val sock = DatagramChannel.open()
            protect(sock.socket())
            val client = InetSocketAddress(serviceIp,servicePort)
            sock.connect(client)
            sock.configureBlocking(false)

            var readCount = 0
            val readBuf = ByteBuffer.allocate(32767)
            while (isActive) try{
                readCount ++

                val tunReadLen = tunRead.read(readBuf.array())
                if(tunReadLen > 0){
                    readCount = 0
                    readBuf.limit(tunReadLen)
                    sock.write(readBuf)
                    readBuf.clear()
                    Log.i("penndev", "sock read from tun : $tunReadLen")
                }

                val sockReadLen = sock.read(readBuf)
                if (sockReadLen > 0){
                    readCount = 0
                    readBuf.limit(tunReadLen)
                    tunWrite.write(readBuf.array(),0,sockReadLen)
                    readBuf.clear()
                    Log.i("penndev", "tun read from sock : $sockReadLen")
                }

                if (readCount > 5){
                    Thread.sleep(100);
                }
            }catch (e: Exception){
                Log.i("penndev", "Job Err: $e")
                return@launch
            }

        }

        return START_NOT_STICKY //super.onStartCommand(intent, flags, startId)
    }

    private fun setupBuilder(): FileDescriptor {
        val builder = Builder()
        localTunnel = builder
            .setMtu(1400)
            .addAddress("10.0.0.200", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("8.8.8.8")
            .establish()
        if(localTunnel == null){
            throw Exception("启动隧道失败")
        }
        return localTunnel!!.fileDescriptor
    }

    /**
     * 转换为前端服务
     */

    private fun setupForeground(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getText(R.string.app_name).toString()
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT))
            var intent = Intent(this, MainActivity::class.java)
            var pendingIntent =  PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
            val notice = Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setContentTitle("服务器标题")
                .setContentText("服务器前台启动中")
                .build()
            startForeground(1, notice)
        }
    }

    /**
     * 初始化验证参数
     */
    private fun setupCommand(intent: Intent?) {
        if(intent == null){
            throw Exception("传参错误")
        }
        // 处理函数校验
        if(intent.getBooleanExtra("close",false)){
            onDestroy()
            throw Exception("关闭成功")
        }
        // 处理系统状态
        if (status){
            throw Exception("正在运行中")
        }
        status = true

        serviceIp = intent.getStringExtra("serviceIp")
        if(serviceIp == null){
            throw Exception("IP错误")
        }

        servicePort = intent.getIntExtra("servicePort", 0)
        if (servicePort <= 0){
            throw Exception("端口错误")
        }
    }


    /**
     * 销毁命令
     */
    override fun onDestroy() {
        Log.d("penndev", "Android TunService onDestroy")
        job?.cancel()
        localTunnel?.close()
        status = false
        stopForeground(true);
        super.onDestroy()
    }

    /**
     * 停止服务
     */
    override fun onRevoke() {
        Log.d("penndev", "Android TunService onRevoke")
        Toast.makeText(this,"Android TunService onRevoke",Toast.LENGTH_LONG).show()
        onDestroy()
    }
}

