package me.dynerowicz.wtest.download

import android.os.AsyncTask
import android.util.Log
import java.io.File
import java.net.URL

import me.dynerowicz.wtest.database.DatabaseManagerService

class FileDownloaderTask (
    private val outputFile: File,
    private val downloadProgressListener: DownloadProgressListener? = null
) : AsyncTask<Unit, Int, Boolean>() {

    override fun onProgressUpdate(vararg values: Int?) {
        if (downloadProgressListener != null && values.isNotEmpty()) {
            val newProgress = values.first()
            newProgress?.let { downloadProgressListener.onDownloadProgressUpdate(newProgress) }
        }
    }

    override fun onPostExecute(result: Boolean) {
        downloadProgressListener?.onDownloadComplete(result)
    }

    override fun doInBackground(vararg p0: Unit?): Boolean {
        val url = URL(DatabaseManagerService.DEFAULT_URL)

        val (contentAvailable, contentLength) = url.retrieveContentLength()

        var contentRetrieved = false
        if(contentAvailable) {
            val filename = url.file.split('/').last()
            Log.v(FileDownloaderTask.TAG, "Downloading file: $filename")

            contentRetrieved = url.retrieveContentBody(contentLength, outputFile, listener = downloadProgressListener)
        }

        return contentRetrieved
    }

    companion object {
        const val TAG: String = "FileDownloaderTask"
    }
}