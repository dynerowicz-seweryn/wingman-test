package me.dynerowicz.wtest.tasks

import android.database.Cursor
import android.util.Log

interface DatabaseQueryListener {
    fun onQueryComplete(cursor: Cursor?) {
        Log.v(TAG, "Query complete, found ${cursor?.count} rows in database")
    }

    fun onQueryCancelled() {
        Log.v(TAG, "Query cancelled")
    }

    companion object {
        const val TAG = "DatabaseQueryListener"
    }
}