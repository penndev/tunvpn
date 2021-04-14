package com.example.tunvpn

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.ContentView


class DatabaseHelper: SQLiteOpenHelper {
    constructor(context: Context) : super(context,"applist",null,1)

    public val tableName = "apps"



    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            db.execSQL("create table "+tableName+" (names varchar(255) not null); ")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun addAllow(appName:String) :Long{
        val cv = ContentValues()
        cv.put("names",appName)
        return writableDatabase.insert(tableName,null,cv)
    }

}