package me.dynerowicz.wtest.tasks

import android.util.Log

interface CsvDownloadListener {
    fun onDownloadUpdate(new: Int) {
        Log.v(TAG, "Download in progress : $new %")
    }

    fun onDownloadComplete(success: Boolean) {
        Log.i(TAG, "Download completed successfully : $success")
    }

    fun onDownloadCancelled() {
        Log.i(TAG, "Download cancelled")
    }

    companion object {
        const val TAG = "DownloadListener"
    }
}