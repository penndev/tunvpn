package com.example.tunvpn

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView

class MainAppsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_apps)

        val apps = findViewById<ListView>(R.id.main_apps)
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appsAdapter = MainAppsAdapter(this,packages)
        apps.adapter = appsAdapter

    }
}