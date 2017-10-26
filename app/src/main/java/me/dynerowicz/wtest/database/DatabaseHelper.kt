package me.dynerowicz.wtest.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(DatabaseContract.CREATE_TABLE_STATEMENT)
        db?.execSQL(DatabaseContract.CREATE_INDEX_STATEMENT)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersionNumber: Int, newVersionNumber: Int) {
        // TODO: implement support for migrating db contents rather than purge them
        if (db != null) {
            db.execSQL(DatabaseContract.DROP_TABLE_STATEMENT)
            onCreate(db)
        }
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "codigosPostais.sqlite3"
    }
}