package me.dynerowicz.wtest

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val search = SearchFragment()
    private val about = AboutFragment()

    private var currentFragment: Fragment = search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search.initialize(this)

        bottomNavigation.setOnNavigationItemSelectedListener(this)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, currentFragment)
                .commit()
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
                if (currentFragment == search)
                    search.dismissKeyboard()
                currentFragment = newFragment
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, currentFragment)
                        .commit()
            }
            return true
        }

        return false
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
