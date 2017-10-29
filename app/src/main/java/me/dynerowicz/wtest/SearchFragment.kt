package me.dynerowicz.wtest

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.dynerowicz.wtest.database.ScrollingCursor
import me.dynerowicz.wtest.presenter.PostalCodeRowAdapter
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.fragment_search.*
import me.dynerowicz.wtest.database.DatabaseHelper


class SearchFragment : Fragment(), View.OnClickListener, View.OnLayoutChangeListener {

    private var recyclerViewAdapter = PostalCodeRowAdapter()

    private lateinit var keyboardManager: InputMethodManager
    private lateinit var layoutManager: LinearLayoutManager

    var dbHelper: DatabaseHelper? = null
    var database: SQLiteDatabase? = null
    var scroller: ScrollingCursor? = null

    fun initialize(context: Context) {
        dbHelper = DatabaseHelper(context)
        database = dbHelper?.readableDatabase
    }

    fun cleanup() {
        database?.close()
        dbHelper?.close()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        keyboardManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        layoutManager = LinearLayoutManager(context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater!!.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_search.setOnClickListener(this)

        field_search_results.addOnLayoutChangeListener(this)
        field_search_results.setHasFixedSize(true)
        field_search_results.adapter = recyclerViewAdapter
        field_search_results.layoutManager = layoutManager
    }

    override fun onLayoutChange(view: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        Log.v(TAG, "onLayoutChange ...")
        if (view == field_search_results) {
            Log.v(TAG, "onLayoutChange in Search Results")
            field_search_results.scrollBy(0, oldBottom - bottom)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            button_search -> {
                performSearch(field_search.text.toString())
                keyboardManager.hideSoftInputFromWindow(field_search.windowToken, 0)
            }
            else -> Log.e(TAG, "Unknown view clicked: ${view?.id}")
        }
    }

    fun dismissKeyboard() {
        keyboardManager.hideSoftInputFromWindow(field_search.windowToken, 0)
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