package com.example.tunvpn

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var editTextIp: EditText
    private lateinit var editTextPort: EditText
    private lateinit var editTextDNS: Spinner

    private var status: Boolean = false

    private val prefs: SharedPreferences
        get() {
            return getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        }

    private val interService: Intent
        get() {
            return Intent(this, TunService::class.java)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        editTextDNS = findViewById(R.id.editText_dns)
        editTextIp = findViewById(R.id.editText_ip)
        editTextPort = findViewById(R.id.editText_port)

        val ip = prefs.getString(getString(R.string.edit_ip), "")
        if (ip != ""){ editTextIp.setText(ip) }
        val port = prefs.getInt(getString(R.string.edit_port), 0)
        if (port > 0){ editTextPort.setText(port.toString()) }

        val editTextDNS: Spinner = findViewById(R.id.editText_dns)
        ArrayAdapter.createFromResource(
                this,
                R.array.dns_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            editTextDNS.adapter = adapter
        }
        editTextDNS.onItemSelectedListener = this

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            1 -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "创建Vpn成功。", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Vpn请求失败[" + resultCode.toString() + "]", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun runBtn(view: View){
        if (status){
            status = false
            startService(interService.putExtra("status", status))
            view.setBackgroundResource(R.drawable.ic_main_run_off)
        } else {
            status = true
            val bundle = Bundle()
            val prefsEdit = prefs.edit()
            if (! editTextIp.text.isEmpty()){
                val ip = editTextIp.text.toString()
                prefsEdit.putString(getString(R.string.edit_ip), ip )
                bundle.putString("ip",ip)
            }
            if (! editTextPort.text.isEmpty() ){
                val port = editTextPort.text.toString().toInt()
                prefsEdit.putInt(getString(R.string.edit_port), port)
                bundle.putInt("port",port)
            }
            prefsEdit.apply()
            val prepare = VpnService.prepare(this)
            if (prepare != null){
                startActivityForResult(prepare, 1)
            }
            bundle.putBoolean("status", status)
            startService(interService.putExtras(bundle))
            view.setBackgroundResource(R.drawable.ic_main_run_on)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val prefsEdit = prefs.edit()
        prefsEdit.putString(getString(R.string.edit_dns), this.resources.getStringArray(R.array.dns_array)[position])
        prefsEdit.apply()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}


}