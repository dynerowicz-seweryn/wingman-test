package me.dynerowicz.wtest.database

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import me.dynerowicz.wtest.R
import me.dynerowicz.wtest.tasks.CsvDownloadListener
import me.dynerowicz.wtest.tasks.CsvDownloadTask
import me.dynerowicz.wtest.tasks.CsvImportListener
import me.dynerowicz.wtest.tasks.CsvImporterTask
import java.io.File
import java.net.URL

class DatabaseInitializerService : Service(), CsvDownloadListener, CsvImportListener {
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var database: SQLiteDatabase

    private lateinit var managerSettings: SharedPreferences
    private var databaseInitialized = false
    private var initializationInProgress = false
    private lateinit var cachedCsvFile: File

    private var downloader: CsvDownloadTask? = null
    private var importer: CsvImporterTask? = null

    // Notification channel
    private val notificationId = 42
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: Notification.Builder

    override fun onCreate() {
        super.onCreate()

        managerSettings = PreferenceManager.getDefaultSharedPreferences(this)

        databaseInitialized = managerSettings.contains(DATABASE_INITIALIZED)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = Notification.Builder(this).apply {
            setContentTitle("Database Initialization")
            setSmallIcon(R.mipmap.ic_launcher_round)
            setProgress(100, 0, true)
        }

        Log.v(TAG, "onCreate [databaseInitialized=$databaseInitialized]")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.v(TAG, "onStartCommand")
        if(!databaseInitialized) {
            if (!initializationInProgress) {
                initializationInProgress = true
                initializeDatabase()
            }
        } else {
            publishReport(operation = INITIALIZATION, status = COMPLETED)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        downloader?.cancel(true)
        importer?.cancel(true)
        super.onDestroy()
    }

    private fun publishReport(operation: String, status: String, percentage: Int? = null) {
        val report = Intent(INITIALIZATION_STATUS).apply {
            putExtra(OPERATION, operation)
            putExtra(STATUS, status)
            percentage?.let {
                putExtra(PROGRESS, percentage)
            }
        }

        notificationBuilder.apply {
            setContentText(prettyString(operation, status, percentage))
            percentage?.let {
                setProgress(100, percentage, false)
            }
        }

        notificationManager.notify(notificationId, notificationBuilder.build())

        sendBroadcast(report)
    }

    private fun initializeDatabase() {
        publishReport(operation = INITIALIZATION, status = STARTING)

        databaseHelper = DatabaseHelper(this)
        database = databaseHelper.writableDatabase

        notificationBuilder.setContentText("Preparing download ...")
        startForeground(notificationId, notificationBuilder.build())

        val csvFile = createTempFile(FILENAME, null, cacheDir)
        cachedCsvFile = csvFile

        publishReport(operation = DOWNLOAD, status = STARTING)

        downloader = CsvDownloadTask(URL(getString(R.string.default_csv_url)), csvFile, downloadListener = this)
        importer = CsvImporterTask(database, cachedCsvFile, importListener = this)

        downloader?.execute()
    }

    private fun terminate() {
        cachedCsvFile.delete()
        database.close()
        databaseHelper.close()
        stopForeground(false)
        stopSelf()
    }

    override fun onDownloadUpdate(percentage: Int) =
        publishReport(operation = DOWNLOAD, status = RUNNING, percentage = percentage)

    override fun onDownloadComplete(success: Boolean) {
        super.onDownloadComplete(success)
        if (success) {
            publishReport(operation = DOWNLOAD, status = COMPLETED)

            publishReport(operation = IMPORT, status = STARTING)

            importer?.execute()
        } else {
            publishReport(operation = DOWNLOAD, status = FAILED)
            terminate()
        }
    }

    override fun onImportProgressUpdate(percentage: Int) =
        publishReport(operation = IMPORT, status = RUNNING, percentage = percentage)

    override fun onImportComplete(result: Pair<Long, Long>) {
        managerSettings.edit().putBoolean(DATABASE_INITIALIZED, true).apply()

        publishReport(operation = IMPORT, status = COMPLETED)
        publishReport(operation = INITIALIZATION, status = COMPLETED)

        terminate()
    }

    override fun onImportCancelled() {
        publishReport(operation = IMPORT, status = FAILED)
        terminate()
    }

    fun prettyString(operation: String, status: String, progressPercentage: Int?): String =
            StringBuilder().apply {
                when(operation) {
                    INITIALIZATION -> {
                        when (status) {
                            STARTING -> append(resources.getString(R.string.InitializationStarting))
                            RUNNING -> append(resources.getString(R.string.InitializationRunning))
                            COMPLETED -> append(resources.getString(R.string.InitializationCompleted))
                            FAILED -> append(resources.getString(R.string.InitializationFailed))
                        }
                    }
                    DOWNLOAD -> {
                        when (status) {
                            STARTING -> append(resources.getString(R.string.DownloadStarting))
                            RUNNING -> append(resources.getString(R.string.DownloadRunning))
                            COMPLETED -> append(resources.getString(R.string.DownloadCompleted))
                            FAILED -> append(resources.getString(R.string.DownloadFailed))
                        }
                    }
                    IMPORT -> {
                        when (status) {
                            STARTING -> append(resources.getString(R.string.ImportStarting))
                            RUNNING -> append(resources.getString(R.string.ImportRunning))
                            COMPLETED -> append(resources.getString(R.string.ImportCompleted))
                            FAILED -> append(resources.getString(R.string.ImportFailed))
                        }
                    }
                }

                if (progressPercentage != null && status == RUNNING)
                    append(" $progressPercentage %")
            }.toString()
    
    companion object {
        private const val TAG = "DatabaseInitializer"
        private const val FILENAME = "postalCodes.csv"
        const val DATABASE_INITIALIZED = "DatabaseInitialized"

        const val INITIALIZATION_STATUS = "me.dynerowicz.wtest.service.DatabaseInitializationService.STATUS"

        const val OPERATION = "OP"
        const val STATUS = "ST"
        const val PROGRESS = "PR"

        const val INITIALIZATION = "INITIALIZATION"
        const val DOWNLOAD = "DOWNLOAD"
        const val IMPORT = "IMPORT"

        const val STARTING = "STARTING"
        const val RUNNING = "RUNNING"
        const val COMPLETED = "COMPLETED"
        const val FAILED = "FAILED"
    }
}