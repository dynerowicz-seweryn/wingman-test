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
    private val settings = SettingsFragment()

    private var currentFragment: Fragment = search

    override fun onStart() {
        super.onStart()

        registerReceiver(dbManagerReceiver, dbManagerIntentFilter)

        dbManagerIntent = Intent(this, DatabaseManagerService::class.java)
        bindService(dbManagerIntent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation.setOnNavigationItemSelectedListener(this)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, currentFragment)
            .commit()
    }

    override fun onStop() {
        unregisterReceiver(dbManagerReceiver)
        unbindService(this)
        super.onStop()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val newFragment: Fragment? =
            when (item.itemId) {
                R.id.navigation_search -> search
                R.id.navigation_settings -> settings
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
                DatabaseManagerService.DB_INITIALIZING ->
                    initializationInProgress = ProgressDialog.show(
                                        context,
                                        context?.getString(R.string.DatabaseInitialization),
                                        context?.getString(R.string.PleaseWait),
                                        true)

                DatabaseManagerService.DB_INITIALIZED -> {
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
