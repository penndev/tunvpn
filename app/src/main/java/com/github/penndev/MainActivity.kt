package com.github.penndev

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    private val allowCreateVpnService = 101

    private val sharedPreferences: SharedPreferences
        get() = getSharedPreferences("tun", Context.MODE_PRIVATE)

    private val intentTunService: Intent
        get() = Intent(this,TunService::class.java)

    private var serverIp: String
        get() = findViewById<EditText>(R.id.input_ip).text.toString()
        set(value) = findViewById<EditText>(R.id.input_ip).setText(value)

    private var serverPort: Int
        get() {
            return try {
                findViewById<EditText>(R.id.input_port).text.toString().toInt()
            } catch (e: NumberFormatException) { // 处理转换异常，例如返回默认端口号
                0
            }
        }
        set(value) = findViewById<EditText>(R.id.input_port).setText(value.toString())

    private var serverPassword: String
        get() = findViewById<EditText>(R.id.input_password).text.toString()
        set(value) = findViewById<EditText>(R.id.input_password).setText(value)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        serverIp = sharedPreferences.getString("serverIp", "192.168.1.1")!!
        val srvPort = sharedPreferences.getInt("serverPort", 0)
        if(srvPort > 0){
            serverPort = srvPort
        }
        serverPassword = sharedPreferences.getString("serverPassword", "")!!
    }

    fun handleAllowApp(view:View) {
        onStartAllowApp()
    }

    fun handleStart(view: View){
        onStartVpn()
    }

    fun handleStop(view: View){
        startService(intentTunService.putExtra("close",true))
    }

    private fun onStartAllowApp() {
        val intent = Intent(this, AllowAppActivity::class.java)
        startActivity(intent)
    }

    private fun onStartVpn() {
        if( serverIp == "" ||
            serverPort == 0 ||
            serverPassword == "" ){
            Toast.makeText(this, "请输入服务器信息", Toast.LENGTH_LONG).show()
            return
        }
        // 存储
        val saveData =  sharedPreferences.edit()
        saveData.putString("serverIp",serverIp)
        saveData.putInt("serverPort",serverPort)
        saveData.putString("serverPassword",serverPassword)
        saveData.apply()

        // 询问vpn权限
        val intentPrepare = VpnService.prepare(this)
        if (intentPrepare != null){
            startActivityForResult(intentPrepare,allowCreateVpnService)
        }else{
            val bundle = Bundle()
            bundle.putString("serviceIp",serverIp)
            bundle.putInt("servicePort",serverPort)
            bundle.putString("servicePassword",serverPassword)
            startService(intentTunService.putExtras(bundle))
        }
    }

    /**
     * 请求用户授权开启vpn的结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            allowCreateVpnService -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "VPN服务创建中...", Toast.LENGTH_LONG).show()
                    onStartVpn()
                } else {
                    Toast.makeText(this, "您拒绝了VPN请求[$resultCode]", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}