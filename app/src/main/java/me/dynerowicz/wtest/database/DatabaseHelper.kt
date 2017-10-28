package me.dynerowicz.wtest.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{

    override fun onCreate(db: SQLiteDatabase?) {
        Log.v(DatabaseHelper::class.java.simpleName, DatabaseContract.CREATE_NORMALIZED_LOCALITY_NAMES_TABLE)
        Log.v(DatabaseHelper::class.java.simpleName, DatabaseContract.CREATE_LOCALITIES_TABLE)
        Log.v(DatabaseHelper::class.java.simpleName, DatabaseContract.CREATE_POSTAL_CODES_TABLE)

        db?.execSQL(DatabaseContract.CREATE_NORMALIZED_LOCALITY_NAMES_TABLE)
        db?.execSQL(DatabaseContract.CREATE_LOCALITIES_TABLE)
        db?.execSQL(DatabaseContract.CREATE_POSTAL_CODES_TABLE)

        Log.v(DatabaseHelper::class.java.simpleName, DatabaseContract.CREATE_POSTAL_CODES_INDEX)
        Log.v(DatabaseHelper::class.java.simpleName, DatabaseContract.CREATE_LOCALITY_NAME_INDEX)

        db?.execSQL(DatabaseContract.CREATE_POSTAL_CODES_INDEX)
        db?.execSQL(DatabaseContract.CREATE_LOCALITY_NAME_INDEX)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersionNumber: Int, newVersionNumber: Int) {
        // TODO: implement support for migrating db contents rather than purge them
        if (db != null) {
            db.execSQL(DatabaseContract.DROP_POSTAL_CODES_TABLE)
            onCreate(db)
        }
    }

    companion object {
        const val DATABASE_VERSION = 4
        const val DATABASE_NAME = "codigosPostais.sqlite3"
    }
}