package me.dynerowicz.wtest

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class AboutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater!!.inflate(R.layout.fragment_about, container, false)

    companion object {
        const val TAG = "AboutFragment"
    }
}