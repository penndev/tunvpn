package com.example.tunvpn

import android.R
import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlin.collections.ArrayList


class MainAppsAdapter(context:Context,apps:List<ApplicationInfo>) : BaseAdapter() {
    private var context = context
    private var apps = apps
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
            newView = View.inflate(context,R.layout.activity_list_item,null)
            viewHolder.appName = newView.findViewById<TextView>(R.id.text1)
            viewHolder.appIcon = newView.findViewById<ImageView>(R.id.icon)
            newView.setTag(viewHolder)
        }else{
            newView = convertView
            viewHolder = newView.getTag() as ViewHolder
        }

        viewHolder.appName.setText( apps[position].loadLabel(pm).toString() )

        viewHolder.appIcon.setImageDrawable(apps[position].loadIcon(pm))

        return newView
    }

    private class ViewHolder{
        lateinit var appName:TextView
        lateinit var appIcon:ImageView
    }

}
