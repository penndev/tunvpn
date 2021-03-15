package com.example.tunvpn

import android.content.Intent
import android.net.VpnService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val interService: Intent
        get() {
            return Intent(this,TunService::class.java)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            1 -> {
                if (resultCode == RESULT_OK){
                    Toast.makeText(this,"创建Vpn成功。", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this,"Vpn请求失败["+ resultCode.toString() +"]", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun startTunService(view: View) {

        val prepare = VpnService.prepare(this)
        if (prepare != null){
            startActivityForResult(prepare,1)
        }
        startService(interService)
    }

    fun stopTunService(view: View) {
        stopService(interService)
    }

}