package me.dynerowicz.wtest

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.navigation_home ->
                fieldMessage.setText(R.string.title_home)

            R.id.navigation_dashboard ->
                fieldMessage.setText(R.string.title_dashboard)

            R.id.navigation_notifications ->
                fieldMessage.setText(R.string.title_notifications)

            else -> return false

        }

        return true
    }
}
