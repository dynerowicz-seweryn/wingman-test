package me.dynerowicz.wtest.tasks

import android.os.AsyncTask
import android.util.Log
import java.io.File
import java.net.URL

import me.dynerowicz.wtest.database.DatabaseManagerService
import me.dynerowicz.wtest.download.retrieveContentBody
import me.dynerowicz.wtest.download.retrieveContentLength

class FileDownloaderTask (
    private val outputFile: File,
    private val downloadProgressListener: DownloadProgressListener? = null
) : AsyncTask<Unit, Int, Boolean>() {

    override fun doInBackground(vararg p0: Unit?): Boolean {
        val url = URL(DatabaseManagerService.DEFAULT_URL)

        val (contentAvailable, contentLength) = url.retrieveContentLength()

        var contentRetrieved = false
        if(contentAvailable) {
            val filename = url.file.split('/').last()
            Log.v(TAG, "Downloading file: $filename")

            contentRetrieved = url.retrieveContentBody(contentLength, outputFile, listener = downloadProgressListener)
        }

        return contentRetrieved
    }

    companion object {
        const val TAG: String = "FileDownloaderTask"
    }
}