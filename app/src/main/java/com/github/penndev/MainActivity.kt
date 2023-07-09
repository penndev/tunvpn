package com.github.penndev

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.penndev.service.Socks5Service
import com.github.penndev.service.TunService

class MainActivity : AppCompatActivity() {
    private val allowCreateVpnService = 101

    private var intentService:Intent? = null

    private val serviceTypeTunName = "tun"

    private val serviceTypeSocks5Name = "socks5"

    private val sharedPreferences: SharedPreferences
        get() = getSharedPreferences("tun", Context.MODE_PRIVATE)

    private val intentTunService: Intent
        get() = Intent(this, TunService::class.java)

    private val intentSocks5Service: Intent
        get() = Intent(this, Socks5Service::class.java)

    private var serverType: String
        set(value) {
            val radioGroup: RadioGroup = findViewById(R.id.input_type)
            when (value) {
                serviceTypeTunName -> radioGroup.check(R.id.input_type_tun)
                serviceTypeSocks5Name -> radioGroup.check(R.id.input_type_socks5)
                else -> radioGroup.check(R.id.input_type_tun)
            }
        }
        get() {
            return findViewById<RadioGroup>(R.id.input_type).checkedRadioButtonId.
            takeIf { it != -1 }?.
            let { radioButtonId ->
                when (radioButtonId) {
                    R.id.input_type_tun -> serviceTypeTunName
                    R.id.input_type_socks5 -> serviceTypeSocks5Name
                    else -> serviceTypeTunName
                }
            } ?: serviceTypeTunName
        }

    private var serverIp: String
        get() = findViewById<EditText>(R.id.input_ip).text.toString()
        set(value) = findViewById<EditText>(R.id.input_ip).setText(value)

    private var serverPort: Int
        get() {
            return try {
                findViewById<EditText>(R.id.input_port).text.toString().toInt()
            } catch (e: NumberFormatException) { // 处理转换异常，例如返回默认端口号
                8000
            }
        }
        set(value) = findViewById<EditText>(R.id.input_port).setText(value.toString())

    private var userName: String
        get() = findViewById<EditText>(R.id.input_username).text.toString()
        set(value) = findViewById<EditText>(R.id.input_username).setText(value)

    private var userPassword: String
        get() = findViewById<EditText>(R.id.input_password).text.toString()
        set(value) = findViewById<EditText>(R.id.input_password).setText(value)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        serverType = sharedPreferences.getString("serverType", "tun")!!
        serverIp = sharedPreferences.getString("serverIp", "")!!
        val srvPort = sharedPreferences.getInt("serverPort", 0)
        if(srvPort > 0){
            serverPort = srvPort
        }
        userName = sharedPreferences.getString("userName", "")!!
        userPassword = sharedPreferences.getString("userPassword", "")!!
    }

    private fun onStartVpn() {
        val intentPrepare = VpnService.prepare(this)
        if (intentPrepare != null){
            startActivityForResult(intentPrepare,allowCreateVpnService)
            return
        }
        if( serverIp == "" || serverPort < 1 ){
            Toast.makeText(this, "请输入服务器信息", Toast.LENGTH_LONG).show()
            return
        }
        //
        val saveData =  sharedPreferences.edit()
        saveData.putString("serverType",serverType)
        saveData.putString("serverIp",serverIp)
        saveData.putInt("serverPort",serverPort)
        saveData.putString("userName",userName)
        saveData.putString("userPassword",userPassword)
        saveData.apply()
        //
        val bundle = Bundle()
        bundle.putString("serviceIp",serverIp)
        bundle.putInt("servicePort",serverPort)
        bundle.putString("userName",userName)
        bundle.putString("userPassword",userPassword)
        when (serverType) {
            serviceTypeTunName -> {
                intentService = intentTunService
                startService(intentTunService.putExtras(bundle))
            }
            serviceTypeSocks5Name -> {
                intentService = intentSocks5Service
                startService(intentSocks5Service.putExtras(bundle))
            }
            else -> Toast.makeText(this, "错误的 serverType", Toast.LENGTH_LONG).show()
        }
    }

    fun handleAllowApp(view:View) {
        onStartAllowApp()
    }

    fun handleStart(view: View){
        onStartVpn()
    }

    fun handleStop(view: View){
        startService(intentService?.putExtra("close",true))
    }

    private fun onStartAllowApp() {
        val intent = Intent(this, AllowAppActivity::class.java)
        startActivity(intent)
    }

    // 请求用户授权开启vpn的结果
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