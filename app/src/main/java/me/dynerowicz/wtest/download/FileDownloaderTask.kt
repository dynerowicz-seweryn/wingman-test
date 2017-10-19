package me.dynerowicz.wtest.download

import android.os.AsyncTask
import android.util.Log
import me.dynerowicz.wtest.MainActivity
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

//TODO: fix to make the server provide the Content-Length
class FileDownloaderTask(private val activity: MainActivity)
    : AsyncTask<URL, Int, Boolean>() {

    lateinit private var outputFile: File

    override fun onPreExecute() {
        super.onPreExecute()
        activity.progressDialog?.show()
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)

        if (values.isNotEmpty()) {
            val progress = values.first()
            if(progress != null)
                activity.progressDialog?.progress = progress
        }
    }

    override fun onPostExecute(downloadCompleted: Boolean) {
        super.onPostExecute(downloadCompleted)

        activity.progressDialog?.dismiss()

        if(downloadCompleted)
            activity.onDownloadSucceeded(outputFile)
        else
            activity.onDownloadFailed()
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

        val filename = url.file.split('/').last()
        Log.v(TAG, "Downloading file: $filename")

        outputFile = createTempFile(filename, null, activity.cacheDir)

        return performDownload(url, outputFile)
    }

    private fun performDownload(url: URL, outputFile: File): Boolean {
        var downloadCompleted = false

        var connection: HttpURLConnection? = null
        val contentLength: Int
        var totalBytesDownloaded = 0

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            //outputFile = activity.openFileOutput(DEFAULT_FILE_NAME, Context.MODE_PRIVATE)
            outputStream = outputFile.outputStream()

            try {
                // Open the connection and proceed with the download only on HTTP 200
                Log.d(TAG, "Downloading file at $url")
                connection = (url.openConnection()).apply {
                    readTimeout = READING_TIMEOUT
                    connectTimeout = CONNECTION_TIMEOUT
                } as HttpURLConnection

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    contentLength = connection.contentLength
                    Log.d(TAG, "Downloading using ${connection.headerFields} ")
                    if(contentLength > 0)
                        activity.progressDialog?.max = 100

                    try {
                        inputStream = BufferedInputStream(connection.inputStream)
                        Log.d(TAG, "Input stream opened")

                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int

                        do {
                            bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)
                            Log.d(TAG, "Bytes read : $bytesRead)")

                            if (bytesRead > 0) {
                                totalBytesDownloaded += bytesRead
                                outputStream.write(buffer, 0, bytesRead)
                            }

                            if(contentLength > 0)
                                publishProgress(totalBytesDownloaded * 100 / contentLength)
                            else
                                publishProgress(totalBytesDownloaded)

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