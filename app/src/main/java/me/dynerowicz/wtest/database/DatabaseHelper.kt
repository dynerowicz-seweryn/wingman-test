package me.dynerowicz.wtest.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{

    override fun onCreate(db: SQLiteDatabase?) {
        Log.v(DatabaseHelper::class.java.simpleName, "Executing: ${DatabaseContract.CREATE_LOCALITY_NAMES_TABLE}")
        Log.v(DatabaseHelper::class.java.simpleName, "Executing: ${DatabaseContract.CREATE_POSTAL_CODES_TABLE}")

        db?.execSQL(DatabaseContract.CREATE_LOCALITY_NAMES_TABLE)

        db?.execSQL(DatabaseContract.CREATE_POSTAL_CODES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersionNumber: Int, newVersionNumber: Int) {
        // TODO: implement support for migrating db contents rather than purge them
        if (db != null) {
            db.execSQL(DatabaseContract.DROP_TABLE_STATEMENT)
            onCreate(db)
        }
    }

    companion object {
        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "codigosPostais.sqlite3"
    }
}