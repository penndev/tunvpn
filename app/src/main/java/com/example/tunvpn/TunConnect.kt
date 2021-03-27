package com.example.tunvpn

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel


class TunConnect(s: TunService): Runnable {
    private val tunService = s

    override fun run() {
        try {
            Log.d("penn","TunConnect.run()")
            val srv = DatagramChannel.open()
            tunService.protect(srv.socket())
            val srvAddr = InetSocketAddress("192.168.0.136", 8000)
            srv.connect(srvAddr)
            srv.configureBlocking(false)

            tunService.setInterFace()
            val fd = tunService.interFace?.getFileDescriptor()

            val tunRead = FileInputStream(fd)
            val tunWrite = FileOutputStream(fd)

            val buf = ByteBuffer.allocate(32767)

            while (true){
                var len = tunRead.read(buf.array())
                if (len > 0){
                    Log.d("penn", "读取发送数据字节：" + len.toString())
                    buf.limit(len)
                    srv.write(buf)
                    buf.clear()
                }

                len = srv.read(buf)
                if (len > 0){
                    Log.d("penn", "写入收到数据字节：" + len.toString())
                    tunWrite.write(buf.array(), 0, len)
                    buf.clear()
                }

            }

        }catch (e: Exception) {
            Log.e("penn", "Get Err:", e);
        }finally {
            Log.d("penn", "TunConnect end");
        }
    }
}