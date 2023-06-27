package com.github.penndev

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView

class AppListAdapter(private val context: Context, private val appList: List<ApplicationInfo>) :
    BaseAdapter() {
    private val selectedItems = SparseBooleanArray()

    override fun getCount(): Int {
        return appList.size
    }

    override fun getItem(position: Int): Any {
        return appList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(
                R.layout.adapter_app_item,
                parent,
                false
            )
            viewHolder = ViewHolder()
            viewHolder.imageView = itemView.findViewById(R.id.imageView)
            viewHolder.textView = itemView.findViewById(R.id.textView)
            viewHolder.checkBox = itemView.findViewById(R.id.checkBox)
            itemView.tag = viewHolder
        } else {
            viewHolder = itemView.tag as ViewHolder
        }
        itemView?.setBackgroundColor(
            if (selectedItems.get(position))
                Color.LTGRAY
            else
                Color.TRANSPARENT
        )
        val appInfo: ApplicationInfo = appList[position]
        viewHolder.imageView.setImageDrawable(appInfo.loadIcon(context.packageManager))
        viewHolder.textView.text = appInfo.loadLabel(context.packageManager)
        // 判断是否选择



        return itemView!!
    }

    fun toggleSelection(position: Int) {
        val isSelected = selectedItems.get(position, false)
        selectedItems.put(position, !isSelected)
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<ApplicationInfo> {
        val selectedApps = mutableListOf<ApplicationInfo>()
        for (i in 0 until selectedItems.size()) {
            val position = selectedItems.keyAt(i)
            if (selectedItems.valueAt(i))
                selectedApps.add(appList[position])
        }
        return selectedApps
    }

    private class ViewHolder {
        lateinit var imageView: ImageView
        lateinit var textView: TextView
        lateinit var checkBox: CheckBox
    }
}
