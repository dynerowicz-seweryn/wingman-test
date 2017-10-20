package me.dynerowicz.wtest

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View

import kotlinx.android.synthetic.main.activity_main.*
import me.dynerowicz.wtest.database.DatabaseManagerService

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var dbManagerIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbManagerIntent = Intent(this, DatabaseManagerService::class.java)

        bottomNavigation.setOnNavigationItemSelectedListener(this)
    }

    fun onClick(view: View?) {
        Log.v(TAG, "onClick: ${view?.id} [${buttonStartService.id}, ${buttonStopService.id}]")

        when(view?.id) {
            buttonStartService.id -> startService(dbManagerIntent)
            buttonStopService.id  -> stopService(dbManagerIntent)
            else -> Log.e(TAG, "Unknown view")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val resourceId =
            when (item.itemId) {
                R.id.navigation_home -> R.string.title_home
                R.id.navigation_dashboard -> R.string.title_dashboard
                R.id.navigation_notifications -> R.string.title_notifications
                else -> -1
            }

        if (resourceId == -1)
            return false

        fieldMessage.setText(resourceId)
        return true
    }
}
