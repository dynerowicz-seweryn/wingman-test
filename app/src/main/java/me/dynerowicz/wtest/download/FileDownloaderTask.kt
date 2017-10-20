package me.dynerowicz.wtest.download

import android.os.AsyncTask
import android.util.Log
import java.io.File
import java.net.URL

import me.dynerowicz.wtest.database.DatabaseManagerService

class FileDownloaderTask (
    private val outputFile: File,
    private val progressListener: ProgressListener? = null
) : AsyncTask<Unit, Int, Pair<Boolean, Int>>() {

    override fun onProgressUpdate(vararg values: Int?) {
        if (progressListener != null && values.isNotEmpty()) {
            val newProgress = values.first()
            newProgress?.let { progressListener.onProgressUpdate(newProgress) }
        }
    }

    override fun onPostExecute(result: Pair<Boolean, Int>) {
        progressListener?.onDownloadComplete(result)
    }

    override fun doInBackground(vararg p0: Unit?): Pair<Boolean, Int> {
        val url = URL(DatabaseManagerService.DEFAULT_URL)

        val (contentAvailable, contentLength) = url.retrieveContentLength()

        var operationResult: Pair<Boolean, Int> = Pair(false, 0)

        if(contentAvailable) {
            val filename = url.file.split('/').last()
            Log.v(FileDownloaderTask.TAG, "Downloading file: $filename")

            operationResult = url.retrieveContentBody(contentLength, outputFile, progressListener = progressListener)
        }

        return operationResult
    }

    companion object {
        const val TAG: String = "FileDownloaderTask"
    }
}