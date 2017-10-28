package me.dynerowicz.wtest.tasks

import android.util.Log

interface CsvImportListener {
    fun onImportProgressUpdate(percentage: Int) {
        Log.v(TAG, "Import in progress : $percentage %")
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