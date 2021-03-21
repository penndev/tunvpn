package com.example.tunvpn

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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class TunService : VpnService() {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    public var interFace:ParcelFileDescriptor? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "TunService 启动", Toast.LENGTH_LONG).show()
        foreground("启动ing", "后台正在启动中..")
        executorService.submit(TunConnect(this))

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        executorService.shutdownNow()
        Toast.makeText(this, "TunService 销毁", Toast.LENGTH_LONG).show()
    }

     @RequiresApi(Build.VERSION_CODES.O)
    private fun foreground(title: String, message: String) {
         val appId = "TunVpn"
         var notifiChannel = NotificationChannel(appId, appId, NotificationManager.IMPORTANCE_DEFAULT)
         notifiChannel.description = "descriptionText"

         val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
         notificationManager.createNotificationChannel(notifiChannel)

         val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
                 PendingIntent.getActivity(this, 0, notificationIntent, 0)
             }
         val notification: Notification = Notification.Builder(this, "TunVpn")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_vpn_conn_notification)
            .setContentIntent(pendingIntent)
            .setTicker("getText(R.string.ticker_text)")
            .build()

         // Notification ID cannot be 0.
         startForeground(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun setInterFace() : Boolean {
        val builder = Builder()
        interFace = builder
                .setMtu(1400)
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("114.114.114.114")
                .establish()

        if (interFace == null){
            foreground("启动fail", "启动失败。")
            return false
        }

        return true
    }

}