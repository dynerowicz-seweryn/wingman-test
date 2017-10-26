package me.dynerowicz.wtest.tasks

import android.database.Cursor
import android.util.Log

interface DatabaseQueryListener {
    fun onQueryComplete(results: Cursor) {
        Log.i(TAG, "Query complete, found ${results.count} rows in database")
    }

    fun onQueryCancelled() {
        Log.i(TAG, "Query cancelled")
    }

    companion object {
        const val TAG = "DatabaseQueryListener"
    }
}