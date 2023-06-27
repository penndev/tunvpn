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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class TunService : VpnService() {
    // status 开启状态
    private var status: Boolean = false
    // tun 设备
    private var localTunnel: ParcelFileDescriptor? = null
    // 工作进程
    private var job: Job? = null

    //远程服务器认证
    private var serviceIp:String = ""
    private var servicePort:Int = 0
    private var servicePassword:String = ""

    private fun setupCommand(intent: Intent?) {
        if(intent == null){
            throw Exception("传参错误")
        }
        if(intent.getBooleanExtra("close",false)){
            onDestroy()
            throw Exception("关闭成功")
        }
        if (status){
            throw Exception("正在运行中")
        }
        status = true
        serviceIp = intent.getStringExtra("serviceIp")!!
        servicePort = intent.getIntExtra("servicePort", 0)
        servicePassword = intent.getStringExtra("servicePassword")!!
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            setupCommand(intent)
        }catch (e: Exception){ //处理抛出异常问题
            Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
            return START_NOT_STICKY
        }
        // 转换为前端服务
        setupForeground()
        // 启动/dev/tun 设备
        //setupBuilder()

        job = GlobalScope.launch {
            while (true){
                delay(5000)
                Log.i("penndev","debug print here123->")

            }
        }

        return START_NOT_STICKY
    }



    //
    //// 创建本地tun设备
    //val tunFd = localTunnel?.fileDescriptor
    //val tunRead = FileInputStream(tunFd)
    //val tunWrite = FileOutputStream(tunFd)
    //
    //// 创建 udp socket
    //val sock = DatagramChannel.open()
    //protect(sock.socket())
    //val client = InetSocketAddress(serviceIp,servicePort)
    //sock.connect(client)
    //sock.configureBlocking(false)
    //
    //var readCount = 0
    //val readBuf = ByteBuffer.allocate(32767)
    //while (isActive) try{
    //    readCount ++
    //
    //    val tunReadLen = tunRead.read(readBuf.array())
    //    if(tunReadLen > 0){
    //        readCount = 0
    //        readBuf.limit(tunReadLen)
    //        sock.write(readBuf)
    //        readBuf.clear()
    //        Log.i("penndev", "sock read from tun : $tunReadLen")
    //    }
    //
    //    val sockReadLen = sock.read(readBuf)
    //    if (sockReadLen > 0){
    //        readCount = 0
    //        readBuf.limit(tunReadLen)
    //        tunWrite.write(readBuf.array(),0,sockReadLen)
    //        readBuf.clear()
    //        Log.i("penndev", "tun read from sock : $sockReadLen")
    //    }
    //
    //    if (readCount > 5){
    //        Thread.sleep(100);
    //    }
    //}catch (e: Exception){
    //    Log.i("penndev", "Job Err: $e")
    //    return@launch
    //}


    //private fun setupBuilder(): FileDescriptor {
    //    val builder = Builder()
    //    localTunnel = builder
    //        .setMtu(1400)
    //        .addAddress("10.0.0.200", 32)
    //        .addRoute("0.0.0.0", 0)
    //        .addDnsServer("8.8.8.8")
    //        .establish()
    //    if(localTunnel == null){
    //        throw Exception("启动隧道失败")
    //    }
    //    return localTunnel!!.fileDescriptor
    //}

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

    override fun onDestroy() {
        job?.cancel()
        localTunnel?.close()
        status = false
        stopForeground(true);
        super.onDestroy()
    }

    override fun onRevoke() {
        Toast.makeText(this,"TunVPN已停止",Toast.LENGTH_LONG).show()
        onDestroy()
    }
}

