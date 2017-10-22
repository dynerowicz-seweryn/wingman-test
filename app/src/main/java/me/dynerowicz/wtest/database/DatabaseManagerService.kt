package me.dynerowicz.wtest.database

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.os.Binder
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import me.dynerowicz.wtest.R
import me.dynerowicz.wtest.tasks.*
import java.io.File
import java.net.URL

class DatabaseManagerService : Service(), CsvDownloadListener, CsvImportListener {
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var database: SQLiteDatabase

    private lateinit var managerSettings: SharedPreferences
    private var databaseInitialized = false

    private var downloader: CsvDownloadTask? = null
    private var importers: Array<CsvImportTask>? = null
    private var importersProgress: LongArray? = null
    private var importersCompleted: Int = 0
    private var numberOfExpectedEntries: Long = 0L

    // Notification channel
    private val notificationId = 42
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: Notification.Builder

    override fun onCreate() {
        super.onCreate()

        databaseHelper = DatabaseHelper(this)

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
        if(!databaseInitialized)
            initializeDatabase()
        else {
            database = databaseHelper.readableDatabase
            sendBroadcast(DatabaseInitializedBroadcast)
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = DatabaseManagerBinder()

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        downloader?.cancel(true)
        importers?.forEach { it.cancel(true) }
        super.onDestroy()
    }

    private fun initializeDatabase() {
        sendBroadcast(DatabaseInitializingBroadcast)

        database = databaseHelper.writableDatabase

        notificationBuilder.setContentText("Preparing download ...")
        startForeground(notificationId, notificationBuilder.build())

        downloader = CsvDownloadTask(
                this,
                URL(getString(R.string.default_csv_url)),
                numberOfCsvEntriesFiles = IMPORTERS_COUNT,
                downloadListener = this
        )

        downloader?.execute()
    }

    override fun onDownloadUpdate(percentage: Int) {
        notificationBuilder.setContentText("Download in progress : $percentage %")
        notificationBuilder.setProgress(100, percentage, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onDownloadFailed() {
        notificationBuilder.setContentText("Download failed. Try again later.")
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onDownloadCancelled() {
        notificationBuilder.setContentText("Download cancelled.")
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onDownloadComplete(totalNumberOfEntries: Long, headerFile: File, entriesFiles: Array<File>) {
        notificationBuilder.setContentText("Preparing CSV import ...")
        notificationBuilder.setProgress(100, 0, true)
        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.i(TAG, "Splitting across ${entriesFiles.size} workers")
        importers = Array(entriesFiles.size) {
            CsvImportTask(database, entriesFiles[it], identifier = it, importListener = this)
        }
        importersProgress = LongArray(entriesFiles.size)
        importersCompleted = 0
        numberOfExpectedEntries = totalNumberOfEntries

        importers?.forEach { it.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR) }
    }

    override fun onImportUpdate(progress: Pair<Int, Long>) {
        Log.v(TAG, "Import progress update $progress")
        val (identifier, importedEntries) = progress

        val progresses = importersProgress
        progresses?.let {
            progresses[identifier] = importedEntries

            val percentage: Int = ((progresses.sum() * 100L) / numberOfExpectedEntries).toInt()
            notificationBuilder.setContentText("Importing CSV entries : $percentage %")
            notificationBuilder.setProgress(100, percentage, false)
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }

    override fun onImportComplete(report: Pair<Long, Long>) {
        notificationBuilder.setContentText("Import complete.")
        notificationBuilder.setProgress(100, 100, false)
        notificationManager.notify(notificationId, notificationBuilder.build())

        managerSettings.edit().putBoolean(DATABASE_INITIALIZED, true).apply()

        stopForeground(true)
        database.close()
        database = databaseHelper.readableDatabase

        sendBroadcast(DatabaseInitializedBroadcast)
    }

    override fun onImportCancelled() {
        notificationBuilder.setContentText("Import cancelled.")
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    inner class DatabaseManagerBinder : Binder() {
        fun getDatabase() = database
    }

    companion object {
        private const val TAG = "DatabaseManagerService"
        private val IMPORTERS_COUNT = maxOf(Runtime.getRuntime().availableProcessors() - 2, 1)

        private const val DATABASE_INITIALIZED = "DatabaseInitialized"

        const val DB_INITIALIZING = "me.dynerowicz.wtest.database.DatabaseManagerService.INITIALIZING"
        const val DB_INITIALIZED  = "me.dynerowicz.wtest.database.DatabaseManagerService.INITIALIZED"

        private val DatabaseInitializingBroadcast = Intent(DB_INITIALIZING)
        private val DatabaseInitializedBroadcast  = Intent(DB_INITIALIZED)
    }
}