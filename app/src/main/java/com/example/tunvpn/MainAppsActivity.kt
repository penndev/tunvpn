package com.example.tunvpn

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*

class MainAppsActivity : AppCompatActivity() {

    val databaseHelp = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_apps)

        val appList = findViewById<ListView>(R.id.main_apps)
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appsAdapter = MainAppsAdapter(this,packages)
        appList.adapter = appsAdapter
        appList.setOnItemClickListener { parent, view, position, id ->
            val itemCB = view.findViewById<CheckBox>(R.id.item_selected)
            itemCB.isChecked = !itemCB.isChecked

            if (itemCB.isChecked){
                val id = databaseHelp.addAllow(packages[position].packageName)
                Log.d("penn","add finish id:"+id.toString())
            }

            Log.d("penn","checkobx status:"+itemCB.isChecked)
        }
        Log.d("penn","I 澳门 ")
    }


}
