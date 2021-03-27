
package com.example.tunvpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class TunService : VpnService() {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(1)

    var interFace:ParcelFileDescriptor? = null

    private var runing = 0

    override fun onCreate() {
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onStart(){
        if (runing == 1){
            return
        }
        foreground("启动中", "正在启动中..")
        Toast.makeText(this, "TunService 启动", Toast.LENGTH_LONG).show()
        executorService.submit(TunConnect(this))
        runing = 1
    }

    private fun destroy(){
        runing = 0
        executorService.shutdownNow()
        Toast.makeText(this, "TunService 销毁", Toast.LENGTH_LONG).show()
        stopSelf()
        interFace?.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val command = intent.getIntExtra("start", -1)
        if (command == 1){
            onStart()
        }else if (command == 0){
            destroy()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() { destroy() }

    fun setInterFace() : Boolean {
        val builder = Builder()
        interFace = builder
                .setMtu(1400)
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("114.114.114.114")
                .establish()

        if (interFace == null){
//            foreground("启动fail", "启动失败。")
            return false
        }
        return true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun foreground(title: String, message: String) {
        val appId = getText(R.string.app_name) as String?
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel(appId, appId, NotificationManager.IMPORTANCE_DEFAULT))

         startForeground(1, Notification.Builder(this, appId)
                 .setContentTitle(title)
                 .setContentText(message)
                 .setSmallIcon(R.drawable.ic_vpn_conn_notification)
                 .setContentIntent(Intent(this, MainActivity::class.java).let {
                     PendingIntent.getActivity(this, 0, it, 0)
                 })
                 .build())
    }


}