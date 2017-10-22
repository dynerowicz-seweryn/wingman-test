package me.dynerowicz.wtest.tasks

import android.util.Log

interface CsvImportListener {
    fun onImportUpdate(progress: Pair<Int, Long>) {
        val (id, entryCount) = progress
        Log.v(TAG, "Import in progress [$id] : $entryCount entries")
    }

    fun onImportComplete(report: Pair<Long, Long>) {
        val (imported, invalid) = report
        Log.v(TAG, "Import completed: $imported entries [invalid=$invalid]")
    }

    fun onImportCancelled() {
        Log.v(TAG, "Import cancelled")
    }

    companion object {
        const val TAG = "CsvImportListener"
    }
}