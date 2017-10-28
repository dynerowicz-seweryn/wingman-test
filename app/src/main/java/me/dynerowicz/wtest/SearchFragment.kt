package me.dynerowicz.wtest

import android.content.Context
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
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager


class SearchFragment : Fragment(), View.OnClickListener {
    private var recyclerViewAdapter = PostalCodeRowAdapter()

    private lateinit var keyboardManager: InputMethodManager

    var database: SQLiteDatabase? = null
    var scroller: ScrollingCursor? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        keyboardManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater!!.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_search.setOnClickListener(this)

        field_search_results.setHasFixedSize(true)
        field_search_results.adapter = recyclerViewAdapter
        field_search_results.layoutManager = LinearLayoutManager(context)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            button_search -> {
                performSearch(field_search.text.toString())
                keyboardManager.hideSoftInputFromWindow(field_search.windowToken, 0)
            }
            else -> Log.e(TAG, "Unknown view clicked: ${p0?.id}")
        }
    }
    
    override fun onDestroyView() {
        keyboardManager.hideSoftInputFromWindow(field_search.windowToken, 0)
        super.onDestroyView()
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