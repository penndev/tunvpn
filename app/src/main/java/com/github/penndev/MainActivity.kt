package com.github.penndev

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    private val intentTunService: Intent
        get() = Intent(this,TunService::class.java)

    private var serverIp: String
        get() = findViewById<EditText>(R.id.input_ip).text.toString()
        set(value) = findViewById<EditText>(R.id.input_ip).setText(value)

    private var serverPort: Int
        get() = findViewById<EditText>(R.id.input_port).text.toString().toInt()
        set(value) = findViewById<EditText>(R.id.input_port).setText(value.toString())

    private var serverPassword: String
        get() = findViewById<EditText>(R.id.input_password).text.toString()
        set(value) = findViewById<EditText>(R.id.input_password).setText(value)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun handleAllowApp(view:View) {
        onStartAllowApp()
    }

    fun handleStart(view: View) {
        val intentPrepare = VpnService.prepare(this)
        if (intentPrepare != null){
            startActivityForResult(intentPrepare,101)
        }else{
            val bundle = Bundle()
            bundle.putString("serviceIp",serverIp)
            bundle.putInt("servicePort",serverPort)
            startService(intentTunService.putExtras(bundle))
        }
    }

    fun handleStop(view: View){
        startService(intentTunService.putExtra("close",true))
    }


    /**
     * 允许部分应用使用tunvpn，或者禁止部分应用使用tunvpn
     */
    private fun onStartAllowApp() {
        val intent = Intent(this, AllowAppActivity::class.java)
        startActivity(intent)
    }


    /**
     * 请求用户授权开启vpn的结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            101 -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "VPN服务创建中...", Toast.LENGTH_LONG).show()
                    handleStart(View(this));
                } else {
                    Toast.makeText(this, "您拒绝了VPN请求[$resultCode]", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}