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
        val packageManager = packageManager
        val installedApps: MutableList<ApplicationInfo> = ArrayList()

        // 获取所有已安装应用的信息
        val allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        // 过滤出非系统应用
        for (app in allApps) {
            if (app.flags and ApplicationInfo.FLAG_SYSTEM <= 0 &&
                //app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0 &&
                app.packageName != packageName ) {
                installedApps.add(app)
            }
        }

        return installedApps
    }


    fun handleBack(view:View) {
        onBackPressed()
    }
}