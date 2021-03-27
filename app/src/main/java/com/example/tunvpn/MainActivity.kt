package com.example.tunvpn

import android.content.Intent
import android.content.SharedPreferences
import android.net.VpnService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val prefs: SharedPreferences
        get() {
            return getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        }

    private val interService: Intent
        get() {
            return Intent(this,TunService::class.java)
        }

    private lateinit var editTextIp: EditText
    private lateinit var editTextPort: EditText

    private var status: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextIp = findViewById(R.id.editText_ip)
        editTextPort = findViewById(R.id.editText_port)
        val ip = prefs.getString(getString(R.string.edit_ip),"")
        if (ip != ""){
            editTextIp.setText(ip)
        }
        val port = prefs.getInt(getString(R.string.edit_port),0)
        if (port > 0){
            editTextPort.setText(port.toString())
        }
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

    fun runBtn(view: View){
        if (status){
            status = false
            startService(interService.putExtra("start",0))
            view.setBackgroundResource(R.drawable.ic_main_run_off)
        } else {
            status = true

            val prefsEdit = prefs.edit()
            if (! editTextPort.text.isEmpty()){
                prefsEdit.putString(getString(R.string.edit_ip), editTextIp.text.toString() )
            }
            if (! editTextIp.text.isEmpty() ){
                prefsEdit.putInt(getString(R.string.edit_port),editTextPort.text.toString().toInt())
            }
            prefsEdit.apply()

            val prepare = VpnService.prepare(this)
            if (prepare != null){
                startActivityForResult(prepare,1)
            }
            startService(interService.putExtra("start",1))
            view.setBackgroundResource(R.drawable.ic_main_run_on)
        }
    }


}