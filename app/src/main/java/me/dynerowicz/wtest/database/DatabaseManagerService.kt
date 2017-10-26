package me.dynerowicz.wtest.database

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Binder
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import me.dynerowicz.wtest.R
import me.dynerowicz.wtest.tasks.CsvImportListener
import me.dynerowicz.wtest.tasks.CsvImporterTask
import me.dynerowicz.wtest.tasks.CsvDownloadListener
import me.dynerowicz.wtest.tasks.CsvDownloadTask
import java.io.File
import java.net.URL

class DatabaseManagerService : Service(), CsvDownloadListener, CsvImportListener {
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
        if(!databaseInitialized) {
            if (!initializationInProgress) {
                initializationInProgress = true
                initializeDatabase()
            }
        } else {
            database = databaseHelper.readableDatabase
            sendBroadcast(DatabaseInitializedBroadcast)
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = DatabaseManagerBinder()

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        downloader?.cancel(true)
        importer?.cancel(true)
        super.onDestroy()
    }

    private fun initializeDatabase() {
        sendBroadcast(DatabaseInitializingBroadcast)

        database = databaseHelper.writableDatabase

        notificationBuilder.setContentText("Preparing download ...")
        startForeground(notificationId, notificationBuilder.build())

        val csvFile = createTempFile(FILENAME, null, cacheDir)
        cachedCsvFile = csvFile

        downloader = CsvDownloadTask(URL(getString(R.string.default_csv_url)), csvFile, downloadListener = this)
        importer = CsvImporterTask(database, cachedCsvFile, importListener = this)

        downloader?.execute()
    }

    override fun onDownloadUpdate(new: Int) {
        notificationBuilder.setContentText("Download in progress : $new %")
        notificationBuilder.setProgress(100, new, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onDownloadComplete(success: Boolean) {
        if (success) {
            notificationBuilder.setContentText("Preparing CSV import ...")
            notificationBuilder.setProgress(100, 0, true)
            notificationManager.notify(notificationId, notificationBuilder.build())

            importer?.execute()
        }
    }

    override fun onDownloadCancelled() {
        notificationBuilder.setContentText("Download cancelled.")
        notificationManager.notify(notificationId, notificationBuilder.build())
        cachedCsvFile.delete()
    }

    override fun onImportProgressUpdate(new: Int) {
        notificationBuilder.setContentText("Importing CSV entries : $new %")
        notificationBuilder.setProgress(100, new, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onImportComplete(result: Pair<Long, Long>) {
        notificationBuilder.setContentText("Import complete.")
        notificationBuilder.setProgress(100, 100, false)
        notificationManager.notify(notificationId, notificationBuilder.build())

        managerSettings.edit().putBoolean(DATABASE_INITIALIZED, true).apply()

        cachedCsvFile.delete()
        stopForeground(true)
        database.close()
        database = databaseHelper.readableDatabase

        sendBroadcast(DatabaseInitializedBroadcast)
    }

    override fun onImportCancelled() {
        notificationBuilder.setContentText("Import cancelled.")
        notificationManager.notify(notificationId, notificationBuilder.build())
        cachedCsvFile.delete()
    }

    inner class DatabaseManagerBinder : Binder() {
        fun getDatabaseHelper(): DatabaseHelper = databaseHelper
        fun getDatabase(): SQLiteDatabase = databaseHelper.readableDatabase
    }

    companion object {
        private const val TAG = "DatabaseManagerService"
        private const val FILENAME = "postalCodes.csv"
        private const val DATABASE_INITIALIZED = "DatabaseInitialized"

        const val DB_INITIALIZING = "me.dynerowicz.wtest.database.DatabaseManagerService.INITIALIZING"
        const val DB_INITIALIZED  = "me.dynerowicz.wtest.database.DatabaseManagerService.INITIALIZED"

        private val DatabaseInitializingBroadcast = Intent(DB_INITIALIZING)
        private val DatabaseInitializedBroadcast  = Intent(DB_INITIALIZED)
    }
}