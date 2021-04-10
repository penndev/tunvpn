package com.example.tunvpn

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editTextIp: EditText
    private lateinit var editTextPort: EditText
    private lateinit var editTextDNS: EditText

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

        editTextDNS = findViewById(R.id.editText_dns)
        editTextIp = findViewById(R.id.editText_ip)
        editTextPort = findViewById(R.id.editText_port)

        val ip = prefs.getString(getString(R.string.edit_ip), "")
        if (ip != ""){ editTextIp.setText(ip) }
        val port = prefs.getInt(getString(R.string.edit_port), 0)
        if (port > 0){ editTextPort.setText(port.toString()) }

        packageList()
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
            if ( editTextDNS.text.isEmpty()){
                val dns = editTextDNS.text.toString()
                prefsEdit.putString(getString(R.string.edit_dns), dns)
                bundle.putString("dns",dns)
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

    fun packageList(){
        startActivity(Intent(this, MainAppsActivity::class.java))
    }
//
//
//    fun packageList(){
//        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
//        for (li in packages){
//            Log.d("penndev",li.packageName)
//            Log.d("penndev",li.sourceDir)
//            Log.d("penndev", packageManager.getApplicationIcon(li.packageName).toString())
//
//        }
//        Log.d("penndev",packages.toString())

//    }

//    final PackageManager pm = getPackageManager();
////get a list of installed apps.
//    List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
//
//    for (ApplicationInfo packageInfo : packages) {
//        Log.d(TAG, "Installed package :" + packageInfo.packageName);
//        Log.d(TAG, "Source dir : " + packageInfo.sourceDir);
//        Log.d(TAG, "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
//    }
//// t
}