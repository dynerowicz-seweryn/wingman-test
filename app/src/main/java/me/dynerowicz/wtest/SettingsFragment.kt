package me.dynerowicz.wtest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import kotlinx.android.synthetic.main.fragment_settings.*
import me.dynerowicz.wtest.database.DatabaseManagerService

class SettingsFragment : Fragment(), View.OnClickListener {
    private lateinit var dbManagerIntent: Intent

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        dbManagerIntent = Intent(context, DatabaseManagerService::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater!!.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonStartService.setOnClickListener(this)
        buttonStopService.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        Log.v(TAG, "onClick: ${view?.id} [${buttonStartService.id}, ${buttonStopService.id}]")

        when(view?.id) {
            buttonStartService.id -> context.startService(dbManagerIntent)
            buttonStopService.id  -> context.stopService(dbManagerIntent)
            else -> Log.e(TAG, "Unknown button")
        }
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}