package me.dynerowicz.wtest

import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import me.dynerowicz.wtest.database.DatabaseManagerService

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, ServiceConnection {

    private lateinit var dbManagerIntent: Intent
    private var dbManagerBinder: DatabaseManagerService.DatabaseManagerBinder? = null

    private val dbManagerReceiver = DbManagerServiceBroadcastReceiver()
    private val dbManagerIntentFilter = IntentFilter()

    init {
        dbManagerIntentFilter.addAction(DatabaseManagerService.DB_INITIALIZED)
        dbManagerIntentFilter.addAction(DatabaseManagerService.DB_INITIALIZING)
    }

    private val search = SearchFragment()
    private val about = AboutFragment()

    private var currentFragment: Fragment = search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation.setOnNavigationItemSelectedListener(this)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, currentFragment)
                .commit()

        dbManagerIntent = Intent(this, DatabaseManagerService::class.java)
        bindService(dbManagerIntent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(dbManagerReceiver, dbManagerIntentFilter)
    }

    override fun onPause() {
        unregisterReceiver(dbManagerReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        unbindService(this)
        super.onDestroy()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val newFragment: Fragment? =
            when (item.itemId) {
                R.id.navigation_search -> search
                R.id.navigation_about -> about
                else -> null
            }

        newFragment?.let {
            if (currentFragment != newFragment) {
                currentFragment = newFragment
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, currentFragment)
                        .commit()
            }
            return true
        }

        return false
    }

    override fun onServiceDisconnected(p0: ComponentName?) = TODO("not implemented")

    override fun onServiceConnected(className: ComponentName?, binder: IBinder?) {
        Log.i(TAG, "onServiceConnected")
        dbManagerBinder = binder as DatabaseManagerService.DatabaseManagerBinder
        startService(dbManagerIntent)
    }

    inner class DbManagerServiceBroadcastReceiver : BroadcastReceiver() {
        private var initializationInProgress: ProgressDialog? = null

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                DatabaseManagerService.DB_INITIALIZING -> {
                    Log.v(TAG, "DatabaseManagerService : Initialization in progress")
                    initializationInProgress = ProgressDialog.show(
                        context,
                        context?.getString(R.string.DatabaseInitialization),
                        context?.getString(R.string.PleaseWait),
                        true)
                }
                DatabaseManagerService.DB_INITIALIZED -> {
                    Log.v(TAG, "DatabaseManagerService : Initialization complete")
                    initializationInProgress?.dismiss()
                    search.database = dbManagerBinder?.getDatabase()
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
