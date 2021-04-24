
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
import kotlin.properties.Delegates


class TunService : VpnService() {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(1)

    private var isRun = false

    public var interFace:ParcelFileDescriptor? = null
    public lateinit var SrvIP:String
    public var SrvPort = -1

    public var VpnAddr = "10.0.0.2"
    public var VpnDNS = "8.8.8.8"

    override fun onCreate() {
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val status = intent.getBooleanExtra("status",false)
        Log.d("penndev", status.toString())
        if (status){
            if (isRun){
                return super.onStartCommand(intent, flags, startId)
            }
            isRun = true
            val status = setInterFace()
            if (status){
                foreground(getString(R.string.notif_succend_title) , getString(R.string.notif_succend_content))
            }else{
                foreground(getString(R.string.notif_fail_title), getString(R.string.notif_fail_content))
            }
            executorService.submit(TunConnect(this))

        }else{
            isRun = false
            destroy()
        }

        return super.onStartCommand(intent, flags, startId)
    }




    private fun destroy(){
        executorService.shutdownNow()
//        Toast.makeText(this, "TunService 销毁", Toast.LENGTH_LONG).show()
        interFace?.close()
        stopSelf()
    }



    override fun onDestroy() { destroy() }

    private fun setInterFace() : Boolean {
        val builder = Builder()
        interFace = builder
                .setMtu(1400)
                .addAddress(VpnAddr, 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer(VpnDNS)
                .establish()

        if (interFace == null){
            return false
        }
        return true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    public fun foreground(title: String, message: String) {
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