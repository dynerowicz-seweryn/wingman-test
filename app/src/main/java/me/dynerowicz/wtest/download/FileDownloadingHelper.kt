package me.dynerowicz.wtest.download

import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

const val TAG = "URL"
const val CONNECTION_TIMEOUT = 3000
const val READING_TIMEOUT = 3000
const val BUFFER_SIZE = 64 * 1024

val NEWLINE = '\n'.toByte()

interface DownloadProgressListener {
    fun onDownloadProgressUpdate(new: Int)
    fun onDownloadComplete(result: Boolean)
}

//TODO: Remove Accept-Encoding=identity to handle the case where the content is gzipped
fun URL.retrieveContentLength(): Pair<Boolean, Long> {
    val connection: HttpURLConnection

    var contentAvailable = false
    var contentLength = -1L

    try {
        // Open the connection and proceed with the download only on HTTP 200
        Log.i(TAG, "Performing HEAD on $this")
        connection = (this.openConnection() as HttpURLConnection).apply {
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

fun URL.retrieveContentBody(contentLength: Long, outputFile: File, listener: DownloadProgressListener? = null): Boolean {
    var downloadCompleted = false

    var connection: HttpURLConnection? = null

    var currentProgress = 0
    var numberOfLines = 0
    var totalBytesDownloaded = 0L

    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null

    try {
        outputStream = outputFile.outputStream()

        try {
            // Open the connection and proceed with the download only on HTTP 200
            Log.d(TAG, "Downloading file at $this")
            connection = (this.openConnection() as HttpURLConnection).apply {
                readTimeout = READING_TIMEOUT
                connectTimeout = CONNECTION_TIMEOUT
                setRequestProperty("Accept-Encoding", "identity")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Downloading using ${connection.headerFields} ")

                //if(contentLength > 0)

                try {
                    inputStream = BufferedInputStream(connection.inputStream)

                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int

                    do {
                        bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)
                        //Log.v(TAG, "Bytes read : $bytesRead)")

                        if (bytesRead > 0) {
                            totalBytesDownloaded += bytesRead
                            for(idx in 0 .. bytesRead)
                                if (buffer[idx] == NEWLINE)
                                    numberOfLines += 1
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
                            listener?.onDownloadProgressUpdate(progressUpdate)
                        }

                    } while (bytesRead != -1)

                    downloadCompleted = true
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