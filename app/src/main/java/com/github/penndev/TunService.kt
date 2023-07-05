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
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel


class TunService : VpnService() {

    // tun 设备
    private var tun: ParcelFileDescriptor? = null
    private lateinit var tunFd: FileDescriptor

    // 工作进程
    private var job: Job? = null

    // status 开启状态
    private var status: Boolean = false

    //远程服务器认证
    private lateinit var serviceSock: DatagramChannel
    private var serviceIp: String = ""
    private var servicePort: Int = 0
    private var servicePassword: String = ""

    //通知
    private val notifyID = 1
    private val notifyChannelID = "tunVpnNotify"
    private val notifyChannelName = "tunVpnNotify"
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationView: RemoteViews

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 处理重复启动，关闭等操作。
        try {
            setupCommand(intent)
            setupVpnServe()
        } catch (e: Exception) { //处理抛出异常问题
            Log.e("penndev", "服务引起异常", e)
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
        return START_NOT_STICKY
    }

    private fun setupCommand(intent: Intent?) {
        if (intent == null) {
            throw Exception("传参错误")
        }
        if (intent.getBooleanExtra("close", false)) {
            onDestroy()
            throw Exception("关闭成功")
        }
        if (status) {
            throw Exception("正在运行中")
        }
        status = true
        serviceIp = intent.getStringExtra("serviceIp")!!
        servicePort = intent.getIntExtra("servicePort", 0)
        servicePassword = intent.getStringExtra("servicePassword")!!
    }

    private fun setService() {
        serviceSock = DatagramChannel.open()
        Log.i("penndev",protect(serviceSock.socket()).toString())
        serviceSock.connect(InetSocketAddress(serviceIp, servicePort))

        tun = Builder()
            .setMtu(1400)
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)
            .addAddress("10.0.0.2", 32)
            .establish() ?: throw Exception("启动隧道失败")
        tunFd = tun?.fileDescriptor!!
    }

    private fun setupVpnServe() {
        setupNotifyForeground() // 初始化启动通知

        job = GlobalScope.launch {
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
        }


    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun setupNotifyForeground() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notifyChannelID, notifyChannelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        //val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        notificationView = RemoteViews(packageName, R.layout.notification_layout)
        notificationView.setTextViewText(R.id.notificationTitle, "TunVPN")
        notificationView.setTextViewText(R.id.notificationContent, "正在连接服务器")

        notificationBuilder = NotificationCompat.Builder(this, notifyChannelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCustomContentView(notificationView)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        startForeground(notifyID, notificationBuilder.build())
    }

    private fun updateNotification(contentText: String) {
        notificationView.setTextViewText(R.id.notificationContent, contentText)
        notificationManager.notify(notifyID, notificationBuilder.build())
    }

    override fun onDestroy() {
        job?.cancel()
        tun?.close()
        status = false
        stopForeground(true)
        super.onDestroy()
    }

    override fun onRevoke() {
        Toast.makeText(this, "TunVPN已停止", Toast.LENGTH_LONG).show()
        onDestroy()
    }
}

