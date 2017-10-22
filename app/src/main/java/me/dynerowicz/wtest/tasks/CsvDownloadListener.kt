package me.dynerowicz.wtest.tasks

import android.util.Log
import java.io.File

interface CsvDownloadListener {
    fun onDownloadUpdate(percentage: Int) {
        Log.v(TAG, "Download in progress : $percentage %")
    }

    fun onDownloadComplete(totalNumberOfEntries: Long, headerFile: File, entriesFiles: Array<File>) {
        Log.v(TAG, "Download completed successfully")
        Log.v(TAG, "Header file: ${headerFile.length()}")
        var sum = headerFile.length()
        entriesFiles.forEach { sum += it.length() }
        Log.v(TAG, "Entry files byte sum: $sum")

        val headerStream = headerFile.inputStream().bufferedReader()
        Log.v(TAG, "Header: ${headerStream.readLine()}")
        headerStream.close()

        entriesFiles.forEach {
            val inputStream = it.inputStream().bufferedReader()
            Log.v(TAG, "Entry file: ${inputStream.readLine()}")
            inputStream.close()
        }
    }

    fun onDownloadFailed() {
        Log.v(TAG, "Download failed")
    }

    fun onDownloadCancelled() {
        Log.v(TAG, "Download cancelled")
    }

    companion object {
        const val TAG = "CsvDownloadListener"
    }
}