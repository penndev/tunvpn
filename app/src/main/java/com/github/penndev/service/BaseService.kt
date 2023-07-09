package com.github.penndev.service

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
import com.github.penndev.MainActivity
import com.github.penndev.R
import kotlinx.coroutines.*

open class BaseService : VpnService() {

    private var status: Boolean = false

    // tun 设备
    protected var tun: ParcelFileDescriptor? = null

    // 工作进程
    protected var job: Job? = null

    //远程服务器认证
    protected var serviceIp: String = ""
    protected var servicePort: Int = 0
    protected var serviceUserName: String = ""
    protected var servicePassword: String = ""

    //通知
    private val notifyID = 1
    private val notifyChannelID = "tunVpnNotify"
    private val notifyChannelName = "tunVpnNotify"
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationView: RemoteViews

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
        serviceUserName =  intent.getStringExtra("userName")!!
        servicePassword = intent.getStringExtra("userPassword")!!
    }

    open fun setupVpnServe() {
        throw RuntimeException("Stub!")
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    protected fun setupNotifyForeground() {
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

    protected fun updateNotification(contentText: String) {
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

