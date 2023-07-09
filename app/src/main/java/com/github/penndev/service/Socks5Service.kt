package com.github.penndev.service

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Socks5Service : BaseService() {

    private fun setupTun() {
        tun = Builder()
            .setMtu(1400)
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)
            .addAddress("192.168.1.1", 32)
            .addDisallowedApplication(packageName)
            .establish() ?: throw Exception("启动隧道失败")
    }

    override fun setupVpnServe() {
        Log.i("penndev", "startService i am here")
        setupNotifyForeground() // 初始化启动通知
        setupTun() // 设置代理设备
        job = GlobalScope.launch {
            try {
                updateNotification("发送通知完成")
                val key = engine.Key()
                //key.mark = 0
                //key.mtu = 0
                key.device = "fd://" + tun?.fd// <--- here
                //key.setInterface("")
                key.logLevel = "warning"
                Log.i("penndev", "socks5://$serviceUserName:$servicePassword@$serviceIp:$servicePort")
                if (serviceUserName != ""){
                    key.proxy = "socks5://$serviceUserName:$servicePassword@$serviceIp:$servicePort"
                }else{

                    key.proxy = "socks5://$serviceIp:$servicePort"
                }
                //key.restAPI = ""
                //key.tcpSendBufferSize = ""
                //key.tcpReceiveBufferSize = ""
                //key.tcpModerateReceiveBuffer = false
                engine.Engine.insert(key)
                engine.Engine.start()
            } catch (e: Exception) { //处理抛出异常问题
                Log.e("penndev", "服务引起异常", e)
            }
        }

    }
}

