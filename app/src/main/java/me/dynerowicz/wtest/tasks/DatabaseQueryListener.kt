package me.dynerowicz.wtest.tasks

import android.util.Log
import me.dynerowicz.wtest.presenter.PostalCodeRow

interface DatabaseQueryListener {
    fun onQueryComplete(postalCodes: List<PostalCodeRow>) {
        Log.i(TAG, "Query complete, found ${postalCodes.size} rows in database")
    }

    fun onQueryCancelled() {
        Log.i(TAG, "Query cancelled")
    }

    companion object {
        const val TAG = "DatabaseQueryListener"
    }
}