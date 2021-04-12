package com.example.tunvpn

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*

class MainAppsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_apps)

        val appList = findViewById<ListView>(R.id.main_apps)
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appsAdapter = MainAppsAdapter(this,packages)
        appList.adapter = appsAdapter
        appList.setOnItemClickListener { parent, view, position, id ->
            val itemCB = view.findViewById<CheckBox>(R.id.item_selected)
            if (itemCB.isChecked){
                itemCB.isChecked = false
            }else{
                itemCB.isChecked = true
            }
        }
    }

}
