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
import me.dynerowicz.wtest.download.FileDownloaderTask
import me.dynerowicz.wtest.download.DownloadProgressListener
import java.io.File

class DatabaseManagerService : Service(), DownloadProgressListener, ImportProgressListener {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var database: SQLiteDatabase

    private lateinit var managerSettings: SharedPreferences
    private var databaseAvailable = false
    private lateinit var cachedCsvFile: File

    // Notification channel
    private val notificationId = 42
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: Notification.Builder

    override fun onCreate() {
        super.onCreate()

        databaseHelper = DatabaseHelper(this)

        managerSettings = PreferenceManager.getDefaultSharedPreferences(this)

        databaseAvailable = managerSettings.contains(DATABASE_AVAILABLE)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = Notification.Builder(this).apply {
            setContentTitle("Database Initialization")
            setSmallIcon(R.mipmap.ic_launcher_round)
            setProgress(100, 0, true)
        }

        Log.v(TAG, "onCreate [databaseAvailable=$databaseAvailable]")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.v(TAG, "onStartCommand")
        if(!databaseAvailable) {

            database = databaseHelper.writableDatabase

            notificationBuilder.setContentText("Preparing download ...")
            startForeground(notificationId, notificationBuilder.build())

            val outputFile = createTempFile(FILENAME, null, cacheDir)
            FileDownloaderTask(outputFile, downloadProgressListener = this).execute()
            cachedCsvFile = outputFile
        }

        database = databaseHelper.readableDatabase

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        cachedCsvFile.delete()
        super.onDestroy()
    }

    override fun onDownloadProgressUpdate(new: Int) {
        notificationBuilder.setContentText("Download in progress : $new %")
        notificationBuilder.setProgress(100, new, false)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onDownloadComplete(result: Boolean) {
        if (result) {
            notificationBuilder.setContentText("Preparing CSV import ...")
            notificationBuilder.setProgress(100, 0, true)
            notificationManager.notify(notificationId, notificationBuilder.build())
            database.importFromCsv(cachedCsvFile, listener = this)
        }
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
        cachedCsvFile.delete()
        managerSettings.edit().putBoolean(DATABASE_AVAILABLE, true).apply()
    }

    companion object {
        const val FILENAME = "postalCodes.csv"
        const val DATABASE_AVAILABLE = "DB_AVAILABLE"
        const val DEFAULT_URL =
                "https://raw.githubusercontent.com" +
                        "/centraldedados" +
                        "/codigos_postais" +
                        "/master" +
                        "/data" +
                        "/codigos_postais.csv"
    }
}