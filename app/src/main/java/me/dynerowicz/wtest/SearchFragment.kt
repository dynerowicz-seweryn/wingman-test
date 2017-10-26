package me.dynerowicz.wtest

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_search.*
import me.dynerowicz.wtest.database.ScrollingCursor
import me.dynerowicz.wtest.presenter.PostalCodeRowAdapter

class SearchFragment : Fragment(), View.OnClickListener {

    private var recyclerViewAdapter = PostalCodeRowAdapter()

    var database: SQLiteDatabase? = null
    var scroller: ScrollingCursor? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater!!.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonSearch.setOnClickListener(this)

        fieldSearchResults.setHasFixedSize(true)
        fieldSearchResults.adapter = recyclerViewAdapter
        fieldSearchResults.layoutManager = LinearLayoutManager(context)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            buttonSearch -> performSearch(field_search.text.toString())
            else -> Log.e(TAG, "Unknown view clicked: ${p0?.id}")
        }
    }

    private fun performSearch(searchInput: String) {
        val localDatabase = database
        localDatabase?.let {
            val resultScroller = ScrollingCursor(localDatabase, recyclerViewAdapter)
            scroller = resultScroller
            resultScroller.inputs = searchInput.split(" ").toTypedArray()

            recyclerViewAdapter.rowScroller = resultScroller
            recyclerViewAdapter.notifyDataSetChanged()

            resultScroller.start()
        }
    }

    companion object {
        const val TAG = "SearchFragment"
    }
}