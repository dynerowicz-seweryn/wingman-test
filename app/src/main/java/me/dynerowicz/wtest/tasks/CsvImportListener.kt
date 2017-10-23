package me.dynerowicz.wtest.tasks

import android.util.Log

interface CsvImportListener {
    fun onImportProgressUpdate(new: Int) {
        Log.v(TAG, "Import in progress : $new %")
    }

    fun onImportComplete(result: Pair<Long, Long>) {
        val (imported, invalid) = result
        Log.i(TAG, "Import completed: $imported entries [invalid=$invalid]")
    }

    fun onImportCancelled() {
        Log.i(TAG, "Import cancelled")
    }

    companion object {
        const val TAG = "CsvImportListener"
    }
}