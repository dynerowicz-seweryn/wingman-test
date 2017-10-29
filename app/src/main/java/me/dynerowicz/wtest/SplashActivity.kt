package me.dynerowicz.wtest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_splash.*
import me.dynerowicz.wtest.database.DatabaseInitializerService

class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"

    private val dbManagerReceiver = DbManagerServiceBroadcastReceiver()
    private val dbManagerIntentFilter = IntentFilter(DatabaseInitializerService.INITIALIZATION_STATUS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val managerSettings = PreferenceManager.getDefaultSharedPreferences(this)
        Log.v(TAG, "Database Initialized ? ${managerSettings.contains(DatabaseInitializerService.DATABASE_INITIALIZED)}")
        if (!managerSettings.contains(DatabaseInitializerService.DATABASE_INITIALIZED))
            startService(Intent(this, DatabaseInitializerService::class.java))
        else
            moveToMainActivity()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(dbManagerReceiver, dbManagerIntentFilter)
    }

    override fun onPause() {
        unregisterReceiver(dbManagerReceiver)
        super.onPause()
    }

    fun moveToMainActivity() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    inner class DbManagerServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                DatabaseInitializerService.INITIALIZATION_STATUS -> {
                    val operation = intent.getStringExtra(DatabaseInitializerService.OPERATION)
                    val status = intent.getStringExtra(DatabaseInitializerService.STATUS)
                    val progress = intent.getIntExtra(DatabaseInitializerService.PROGRESS, -1)

                    val pretty = prettyString(operation, status, progress)
                    Log.v(tag, "DatabaseInitializationService: Initialization status: $pretty")

                    if (operation == DatabaseInitializerService.INITIALIZATION && status == DatabaseInitializerService.COMPLETED) {
                        Thread.sleep(500)
                        moveToMainActivity()
                    } else {
                        progress_initialization.visibility = View.VISIBLE
                        field_initialization_status.text = pretty
                    }
                }
            }
        }
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
        val tag = SplashActivity::class.java.simpleName

        const val INITIALIZATION = "INITIALIZATION"
        const val DOWNLOAD = "DOWNLOAD"
        const val IMPORT = "IMPORT"

        const val STARTING = "STARTING"
        const val RUNNING = "RUNNING"
        const val COMPLETED = "COMPLETED"
        const val FAILED = "FAILED"
    }
}
