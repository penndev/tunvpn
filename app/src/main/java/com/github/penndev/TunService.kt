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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer


class TunService : VpnService() {
    // tun 设备
    private var localTunnel: ParcelFileDescriptor? = null

    // 工作进程
    private var job: Job? = null

    // 远程服务器IP地址
    private var serviceIp: String? = null

    // 远程服务器端口
    private var servicePort: Int = 0


    ///**
    // * 进行服务器连接
    // */
    //private fun handleSetup() {
    //    job = GlobalScope.launch {
    //        Log.i("penndev", "onCreate:  start")
    //        Thread.sleep(5000)
    //        Log.i("penndev", "onCreate:  finish")
    //    }
    //}

    override fun onCreate() {
        Log.i("penndev", "Android TunService onCreate")
        super.onCreate()
    }

    /**
     * 启动命令
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("penndev", "Android TunService onStartCommand")

        // 处理启动的参数
        if(intent == null){
            Toast.makeText(this,"传参错误",Toast.LENGTH_LONG).show()
            return START_NOT_STICKY
        }

        serviceIp = intent.getStringExtra("serviceIp")
        if(serviceIp == null){
            Toast.makeText(this,"IP错误",Toast.LENGTH_LONG).show()
            return START_NOT_STICKY
        }

        servicePort = intent.getIntExtra("servicePort", 0)
        if (servicePort <= 0){
            Toast.makeText(this,"端口错误",Toast.LENGTH_LONG).show()
            return START_NOT_STICKY
        }

        // 转换为前端服务
        val channelId = getText(R.string.app_name).toString()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT))

        val pendingIntent = Intent(this, MainActivity::class.java).let {intent ->
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        }
        val notice = Notification.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("服务器标题").setContentText("服务器前台启动中")
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notice)

        //
        val builder = Builder()
        localTunnel = builder
            .addAddress("192.168.2.2", 24)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("192.168.1.1")
            .establish()



        //job = GlobalScope.launch {

            try {
                Log.i("penndev", "job:  start")

                var tunRead = FileInputStream(localTunnel.fileDescriptor)
                var tunWrite = FileOutputStream(localTunnel.fileDescriptor)
                val buf = ByteBuffer.allocate(32767)
                while (true){
                    val len = tunRead.read(buf.array())
                    Log.i("penndev", "tun read from tun : $len")
                }
                // 创建socket
                //var sockFd = Socket(serviceIp,servicePort)
                //protect(sockFd)

            }catch (e: Exception){
                Toast.makeText(this@TunService,"vpn连接错误",Toast.LENGTH_LONG).show()
                Log.i("penndev", "Job Err: $e")
            }finally {
                Log.i("penndev", "job:  finally")
            }

        //}

        return START_NOT_STICKY //super.onStartCommand(intent, flags, startId)
    }

    /**
     * 销毁命令
     */
    override fun onDestroy() {
        Log.d("penndev", "Android TunService onDestroy")

        job?.cancel()
        localTunnel?.close()
        super.onDestroy()
    }

    /**
     * 停止服务
     */
    override fun onRevoke() {
        Toast.makeText(this,"我知道你从其他地方关闭了vpn",Toast.LENGTH_LONG).show()
    }
}

