package me.dynerowicz.wtest.tasks

import android.os.AsyncTask
import android.util.Log
import java.net.URL

import java.io.*
import java.net.HttpURLConnection

class CsvDownloadTask(
        private val url: URL,
        private val outputFile: File,
        private val downloadListener: CsvDownloadListener? = null
) : AsyncTask<Unit, Int, Boolean>() {

    override fun onPostExecute(downloadCompleted: Boolean) {
        super.onPostExecute(downloadCompleted)
        downloadListener?.onDownloadComplete(downloadCompleted)
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        if(values.isNotEmpty()) {
            val progressUpdate = values.first()
            if(progressUpdate != null)
                downloadListener?.onDownloadUpdate(progressUpdate)
        }
    }

    override fun onCancelled() {
        downloadListener?.onDownloadCancelled()
    }

    override fun doInBackground(vararg p0: Unit?): Boolean {
        val (contentAvailable, contentLength) = retrieveContentLength(url)

        var contentRetrieved = false
        if(contentAvailable) {
            val filename = url.file.split('/').last()
            Log.v(TAG, "Downloading file: $filename")

            contentRetrieved = retrieveContentBody(url, contentLength, outputFile)
        }

        return contentRetrieved
    }

    //TODO: Remove Accept-Encoding=identity to handle the case where the content is gzipped
    private fun retrieveContentLength(url: URL): Pair<Boolean, Long> {
        val connection: HttpURLConnection

        var contentAvailable = false
        var contentLength = -1L

        try {
            // Open the connection and proceed with the download only on HTTP 200
            Log.i(TAG, "Performing HEAD on $this")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                setRequestProperty("Accept-Encoding", "identity")
                connectTimeout = CONNECTION_TIMEOUT
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                contentAvailable = true
                contentLength = connection.contentLength.toLong()
                Log.v(TAG, "Full headers: ${connection.headerFields} ")
                Log.d(TAG, "Content-Length: $contentLength")
            } else
                Log.e(TAG, "Response code: ${connection.responseCode}")
        } catch (ioe: IOException) {
            Log.e(TAG, "Connection error: $ioe")
        }

        return Pair(contentAvailable, contentLength)
    }

    private fun retrieveContentBody(url: URL, contentLength: Long, outputFile: File): Boolean {
        var downloadCompleted = false

        var connection: HttpURLConnection? = null

        var currentProgress = 0
        var totalBytesDownloaded = 0L

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            outputStream = outputFile.outputStream()

            try {
                // Open the connection and proceed with the download only on HTTP 200
                Log.d(TAG, "Downloading file at $this")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    readTimeout = READING_TIMEOUT
                    connectTimeout = CONNECTION_TIMEOUT
                    setRequestProperty("Accept-Encoding", "identity")
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Downloading using ${connection.headerFields} ")

                    try {
                        inputStream = BufferedInputStream(connection.inputStream)

                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int

                        do {
                            bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)

                            if (bytesRead > 0) {
                                totalBytesDownloaded += bytesRead
                                outputStream.write(buffer, 0, bytesRead)
                            }

                            val progressUpdate: Int =
                                    if (contentLength > 0)
                                        (totalBytesDownloaded * 100L / contentLength).toInt()
                                    else
                                        totalBytesDownloaded.toInt()

                            if (progressUpdate != currentProgress) {
                                Log.v(TAG, "ProgressUpdate: $totalBytesDownloaded bytes / $contentLength bytes [$progressUpdate%]")
                                currentProgress = progressUpdate
                                publishProgress(currentProgress)
                            }

                        } while (bytesRead != -1 && !isCancelled)

                        downloadCompleted = (totalBytesDownloaded == contentLength)
                    } catch (io: IOException) {
                        Log.e(TAG, io.toString())
                    } finally {
                        inputStream?.close()
                    }
                } else
                    Log.e(TAG, "Request failed with code ${connection.responseCode}")
            } catch (io: IOException) {
                Log.e(TAG, io.toString())
            } finally {
                connection?.disconnect()
            }
        } catch (fnfe: FileNotFoundException) {
            // Given the parameters to openFileOutput(), When can this exception occur ?
            Log.e(TAG, fnfe.toString())
        } finally {
            // If the output file was successfully opened, close it and delete the partial content if the download did not succeed
            outputStream?.close()
        }

        Log.i(TAG, "Total bytes downloaded: $totalBytesDownloaded")
        Log.i(TAG, "Saved to : ${outputFile.name}")

        return downloadCompleted
    }

    companion object {
        const val TAG: String = "CsvDownloadTask"
        const val CONNECTION_TIMEOUT = 3000
        const val READING_TIMEOUT = 3000
        const val BUFFER_SIZE = 64 * 1024
    }
}