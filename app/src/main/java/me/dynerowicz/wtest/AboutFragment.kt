package me.dynerowicz.wtest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import me.dynerowicz.wtest.database.DatabaseManagerService

class AboutFragment : Fragment() {
    private lateinit var dbManagerIntent: Intent

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        dbManagerIntent = Intent(context, DatabaseManagerService::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater!!.inflate(R.layout.fragment_about, container, false)

    companion object {
        const val TAG = "AboutFragment"
    }
}