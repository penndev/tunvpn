package com.example.tunvpn

import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView


class MainAppsAdapter(context:Context,apps:List<ApplicationInfo>,selected:List<String>?): BaseAdapter() {
    private val context = context
    private val apps = apps
    private val selected = selected
    private val pm = context.getPackageManager()
    override fun getCount(): Int {
        return apps.size
    }

    override fun getItem(position: Int): Any {
        return apps.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var newView:View
        var viewHolder = ViewHolder()
        if (convertView == null) {
            newView = View.inflate(context, R.layout.activity_main_app_item ,null)
            viewHolder.appName = newView.findViewById<TextView>(R.id.item_appname)
            viewHolder.appIcon = newView.findViewById<ImageView>(R.id.item_appicon)
            viewHolder.appSeled = newView.findViewById<CheckBox>(R.id.item_selected)
            newView.setTag(viewHolder)
        }else{
            newView = convertView
            viewHolder = newView.getTag() as ViewHolder
        }
        if (selected != null) {
            if (selected.contains(apps[position].packageName)){
                viewHolder.appSeled.isChecked = true
            }
        }
        viewHolder.appName.setText( apps[position].loadLabel(pm).toString() )
        viewHolder.appIcon.setImageDrawable(apps[position].loadIcon(pm))
        return newView
    }

    private class ViewHolder{
        lateinit var appSeled: CheckBox
        lateinit var appName:TextView
        lateinit var appIcon:ImageView
    }


}
