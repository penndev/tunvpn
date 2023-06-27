package com.github.penndev

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity


class AllowAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allowapp)
        val appListView = findViewById<ListView>(R.id.appList)
        val adapter = AppListAdapter(this,  getInstalledApps() )
        appListView.adapter = adapter
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun getInstalledApps(): List<ApplicationInfo> {
        val installedApps: MutableList<ApplicationInfo> = ArrayList()
        val allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in allApps) {
            val isNoSelf =  app.packageName != packageName
            if (isNoSelf) {
                installedApps.add(app)
            }
        }
        return installedApps
    }


    fun handleBack(view:View) {
        onBackPressed()
    }
}