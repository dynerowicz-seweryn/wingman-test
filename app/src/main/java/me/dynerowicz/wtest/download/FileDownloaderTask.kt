package me.dynerowicz.wtest.download

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.zip.GZIPInputStream

//TODO: Remove Accept-Encoding to handle the case where the content is gzipped
class FileDownloaderTask(private val context: Context)
    : AsyncTask<URL, Int, Boolean>() {

    lateinit private var outputFile: File

    override fun onPreExecute() {
        super.onPreExecute()
        //activity.progressDialog.show()
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)

        if (values.isNotEmpty()) {
            val progress = values.first()
            progress?.let {
                //activity.progressDialog.progress = progress
            }
        }
    }

    override fun onPostExecute(downloadCompleted: Boolean) {
        super.onPostExecute(downloadCompleted)

        //activity.progressDialog.dismiss()

        //if(downloadCompleted)
        //    activity.onFileAvailable(outputFile)
        //else
        //    activity.onDownloadFailed()
    }

    // TODO: handle resuming the download
    override fun doInBackground(vararg urls: URL): Boolean {
        val url = when (urls.size) {
            0 -> URL(DEFAULT_URL)
            1 -> urls.first()
            else -> {
                Log.e(TAG, "No support for downloading multiple files")
                return false
            }
        }

        val (contentAvailable, contentLength) = retrieveContentLength(url)

        var contentRetrieved = false
        if(contentAvailable) {
            val filename = url.file.split('/').last()
            Log.v(TAG, "Downloading file: $filename")

            outputFile = createTempFile(filename, null, context.cacheDir)

            contentRetrieved = retrieveContentBody(url, contentLength, outputFile)
        }

        return contentRetrieved
    }

    private fun retrieveContentLength(url: URL): Pair<Boolean, Long> {
        val connection: HttpURLConnection

        var contentAvailable = false
        var contentLength = -1L

        try {
            // Open the connection and proceed with the download only on HTTP 200
            Log.i(TAG, "Performing HEAD on $url")
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
                Log.d(TAG, "Downloading file at $url")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    readTimeout = READING_TIMEOUT
                    connectTimeout = CONNECTION_TIMEOUT
                    setRequestProperty("Accept-Encoding", "identity")
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Downloading using ${connection.headerFields} ")

                    //if(contentLength > 0)
                        //activity.progressDialog.max = 100

                    try {
                        inputStream = BufferedInputStream(connection.inputStream)

                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int

                        do {
                            bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)
                            //Log.v(TAG, "Bytes read : $bytesRead)")

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
                                publishProgress(progressUpdate)
                            }

                        } while (bytesRead != -1 && !isCancelled)

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
            outputStream?.apply {
                close()
                if(!downloadCompleted)
                    outputFile.delete()
            }
        }

        Log.i(TAG, "Total bytes downloaded: $totalBytesDownloaded")
        Log.i(TAG, "Saved to : ${outputFile.name}")

        return downloadCompleted
    }

    companion object {
        const val TAG: String = "FileDownloaderTask"

        const val CONNECTION_TIMEOUT = 3000
        const val READING_TIMEOUT = 3000
        const val BUFFER_SIZE = 64 * 1024
        const val DEFAULT_URL =
                "https://raw.githubusercontent.com" +
                        "/centraldedados" +
                        "/codigos_postais" +
                        "/master" +
                        "/data" +
                        "/codigos_postais.csv"
    }
}