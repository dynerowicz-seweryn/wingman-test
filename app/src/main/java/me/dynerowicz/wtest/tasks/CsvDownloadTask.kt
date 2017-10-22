package me.dynerowicz.wtest.tasks

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class CsvDownloadTask(
        private val context: Context,
        private val url: URL,
        private val numberOfCsvEntriesFiles: Int = 1,
        private val downloadListener: CsvDownloadListener? = null
) : AsyncTask<Unit, Long, Boolean>() {

    private lateinit var headerFile: File
    private lateinit var entriesFiles: Array<File>
    private var totalNumberOfEntries = 0L

    private var contentLength: Long = -1L
        set(value) {
            field = value
            contentAvailable = true
            contentLengthKnown = (contentLength >= 0)
            contentPerFile = contentLength / numberOfCsvEntriesFiles

            headerFile = createTempFile(FILENAME, HEADER, context.cacheDir)
            // Create the necessary files
            entriesFiles = Array (if (contentLengthKnown) numberOfCsvEntriesFiles else 1) {
                createTempFile(FILENAME, null, context.cacheDir)
            }

            Log.d(TAG, "Content-Length: $contentLength, ContentPerFile: $contentPerFile (relevant if contentLength >= 0)")
        }
    private var contentLengthKnown : Boolean = false
    private var contentAvailable: Boolean = false
    private var contentPerFile: Long = -1L

    init {
        if (numberOfCsvEntriesFiles < 1)
            throw IllegalArgumentException("Number of files cannot be less than 1 [given $numberOfCsvEntriesFiles]")
    }

    private var currentProgressPercentage = 0
    override fun onProgressUpdate(vararg values: Long?) {
        super.onProgressUpdate(*values)
        if(values.isNotEmpty()) {
            val progressUpdate = values.first()

            if(progressUpdate != null && contentLength > 0) {
                val newProgressPercentage = ((progressUpdate * 100L) / contentLength).toInt()
                if(currentProgressPercentage != newProgressPercentage) {
                    currentProgressPercentage = newProgressPercentage
                    downloadListener?.onDownloadUpdate(currentProgressPercentage)
                }
            }
        }
    }

    override fun onPostExecute(downloadCompleted: Boolean) {
        super.onPostExecute(downloadCompleted)
        downloadListener?.onDownloadComplete(totalNumberOfEntries, headerFile, entriesFiles)
    }

    override fun onCancelled() {
        downloadListener?.onDownloadCancelled()
    }

    override fun doInBackground(vararg p0: Unit?): Boolean {
        var headConnection: HttpURLConnection? = null
        try {
            // Open the connection and proceed with the download only on HTTP 200
            Log.i(TAG, "Performing HEAD on $this")
            headConnection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                connectTimeout = CONNECTION_TIMEOUT
                // TODO: fix this to use GZip when possible. Somehow ...
                setRequestProperty("Accept-Encoding", "identity")
                connect()
            }

            if (headConnection.responseCode == HttpURLConnection.HTTP_OK) {
                contentLength = headConnection.contentLength.toLong()
            } else
                Log.e(TAG, "Response code: ${headConnection.responseCode}")

        } catch (ioe: IOException) {
            Log.e(TAG, "Connection error: $ioe")
        } finally {
            headConnection?.disconnect()
        }

        var downloadCompleted = false
        var totalBytesDownloaded = 0L

        val headerStream = headerFile.outputStream().bufferedWriter()
        val outputStreams = Array(entriesFiles.size) { index -> entriesFiles[index].bufferedWriter() }

        try {
            // Open the connection and proceed with the download only on HTTP 200
            Log.d(TAG, "Downloading file at $this")
            val bodyConnection = (url.openConnection() as HttpURLConnection).apply {
                readTimeout = READING_TIMEOUT
                connectTimeout = CONNECTION_TIMEOUT
                setRequestProperty("Accept-Encoding", "identity")
                connect()
            }

            if (bodyConnection.responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Downloading using ${bodyConnection.headerFields} ")

                val inputStream = bodyConnection.inputStream.bufferedReader()
                try {
                    // Not an accurate measure given the way Unicode maps characters to bytes. Still, good enough.
                    var currentLine = inputStream.readLine()

                    if (currentLine != null) {
                        totalBytesDownloaded += currentLine.length + 1
                        headerStream.write(currentLine)
                        headerStream.newLine()

                        var currentOutputStreamIndex = 0
                        var bytesWrittenToCurrentOutput = 0L

                        while (currentLine != null && !isCancelled) {
                            currentLine = inputStream.readLine()
                            if (currentLine != null) {
                                totalNumberOfEntries += 1
                                totalBytesDownloaded += currentLine.length + 1

                                outputStreams[currentOutputStreamIndex].write(currentLine)
                                outputStreams[currentOutputStreamIndex].newLine()

                                bytesWrittenToCurrentOutput += currentLine.length + 1

                                if (contentLengthKnown && bytesWrittenToCurrentOutput > contentPerFile) {
                                    bytesWrittenToCurrentOutput = 0
                                    currentOutputStreamIndex += 1
                                }

                                publishProgress(totalBytesDownloaded)
                            }
                        }
                    }

                    if (currentLine == null) {
                        downloadCompleted = true
                        publishProgress(contentLength)
                    }

                } catch (io: IOException) {
                    Log.e(TAG, io.toString())
                } finally {
                    inputStream.close()
                }
            } else
                Log.e(TAG, "Request failed with code ${bodyConnection.responseCode}")
        } catch (io: IOException) {
            Log.e(TAG, io.toString())
        } finally {
            headerStream.close()
            outputStreams.forEach { it.close() }
        }

        Log.i(TAG, "Estimated total bytes downloaded: $totalBytesDownloaded split into ${entriesFiles.size}")

        return downloadCompleted
    }

    companion object {
        const val TAG: String = "FileDownloaderTask"
        const val CONNECTION_TIMEOUT = 5000
        const val READING_TIMEOUT = 10000

        private const val FILENAME = "postalCodes"
        private const val HEADER = "headers"
    }
}