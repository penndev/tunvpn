package com.example.tunvpn

import android.net.VpnService
import android.os.Handler
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel


class TunConnect(s:TunService ): Runnable {
    private val tunService = s
    override fun run() {
        try {
            val srv = DatagramChannel.open()
            val srvAddr = InetSocketAddress("192.168.0.136", 8000)
            srv.connect(srvAddr)

            val tunDev = tunService.getInterFace()
            val tunRead = FileInputStream(tunDev?.fileDescriptor).channel
            val tunWrite = FileOutputStream(tunDev?.fileDescriptor).channel
//
            val buf = ByteBuffer.allocate(16384)
            Log.d("penn", "I am here...");
            while (true){
                var len = tunRead.read(buf)
                srv.write(buf)
                buf.clear()

                len = srv.read(buf)
                tunWrite.write(buf)
                buf.clear()
            }

        }catch (e: Exception) {
            Log.e("penn", "Get Err:", e);
        }finally {
            Log.d("penn", "TunConnect end");
        }
    }
}