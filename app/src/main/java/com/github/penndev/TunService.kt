package com.github.penndev

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel


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

    //通知
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "tunVpnNotify"
    private val CHANNEL_NAME = "tunVpnNotify"

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationView: RemoteViews


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

    private fun setupVpnServe(){
        setupNotifyForeground() // 初始化启动通知


        job = GlobalScope.launch {
            // 避免线程阻塞
            val sock = DatagramChannel.open()
            protect(sock.socket())
            val client = InetSocketAddress(serviceIp,servicePort)
            sock.connect(client)

            // 获取远程认证。并获取本地的IP配置dns等等。
            localTunnel = Builder()
                .addRoute("0.0.0.0", 0)
                .addAddress("10.0.0.200", 32)
                .establish()
            if(localTunnel == null){
                throw Exception("启动隧道失败")
            }

            val tunFd = localTunnel?.fileDescriptor
            val tunRead = FileInputStream(tunFd)

            //主要内容
            val readBuf = ByteBuffer.allocate(32767)
            while (isActive) try{
                readBuf.clear()
                val readBufLen = withContext(Dispatchers.IO) {
                    tunRead.read(readBuf.array())
                }
                if(readBufLen > 0){
                    readBuf.limit(readBufLen)
                    sock.write(readBuf)
                }
            }catch (e: Exception){
                Log.i("penndev", "Job Err: $e")
                return@launch
            }
        }

        //
        //job = CoroutineScope(Dispatchers.Default).launch {
        //    val readBuf = ByteBuffer.allocate(32767)
        //    while (true){

        //    }
        //}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 处理重复启动，关闭等操作。
        try {
            setupCommand(intent)
            setupVpnServe()
        }catch (e: Exception){ //处理抛出异常问题
            Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
        }
        return START_NOT_STICKY
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun setupNotifyForeground() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationView = RemoteViews(packageName, R.layout.notification_layout)
        notificationView.setTextViewText(R.id.notificationTitle, "TunVPN")
        notificationView.setTextViewText(R.id.notificationContent, "正在连接服务器")

        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCustomContentView(notificationView)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateNotification(contentText: String) {
        notificationView.setTextViewText(R.id.notificationContent, contentText)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onDestroy() {
        job?.cancel()
        localTunnel?.close()
        status = false
        stopForeground(true)
        super.onDestroy()
    }

    override fun onRevoke() {
        Toast.makeText(this,"TunVPN已停止",Toast.LENGTH_LONG).show()
        onDestroy()
    }
}

