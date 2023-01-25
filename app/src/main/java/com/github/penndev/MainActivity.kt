package com.github.penndev

import android.content.Intent
import android.net.VpnService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val intentTunService: Intent
        get(){
            return Intent(this,TunService::class.java)
        }

    private var serverIp: String
        get() = findViewById<EditText>(R.id.input_ip).text.toString()
        set(value){
            findViewById<EditText>(R.id.input_ip).setText(value)
        }

    private var serverPort: Int
        get() = findViewById<EditText>(R.id.input_port).text.toString().toInt()
        set(value){
            findViewById<EditText>(R.id.input_port).setText(value.toString())
        }


    private var serverPassword: String
        get() = findViewById<EditText>(R.id.input_password).text.toString()
        set(value){
            findViewById<EditText>(R.id.input_password).setText(value)
        }


    /**
     * 初始化UI入口
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * 监听启动
     */
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

    /**
     * 监听停止
     */
    fun handleStop(view: View){
        startService(intentTunService.putExtra("close",true))
    }

    /**
     * 授权vpn回调。
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