 package com.example.tunvpn

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.annotation.ContentView


class DatabaseHelper: SQLiteOpenHelper {
    constructor(context: Context) : super(context,"applist.db",null,1)

    public val tableName = "apps"



    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            db.execSQL("create table "+tableName+" (name varchar(255) not null); ")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun addAllow(appName:String) :Long{
        val cv = ContentValues()
        cv.put("name",appName)
        return writableDatabase.insert(tableName,null,cv)
    }

    fun delAllow(appName: String):Int{
        var arg = arrayOf(appName)
        Log.d("penn",arg.toString())
        return writableDatabase.delete(tableName,"name=?",arg)
    }

    fun getAllow(): ArrayList<String>? {
        val cursor = writableDatabase.query(tableName,null,null,null,null,null,null)

        if (cursor.moveToFirst() == false){
            cursor.close()
            return null
        }

        var apps = ArrayList<String>()
        do{
            apps.add(cursor.getString(cursor.getColumnIndex("name")))
        }while (cursor.moveToNext())
        cursor.close()
        return apps
    }

}