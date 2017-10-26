package me.dynerowicz.wtest

import android.app.ProgressDialog
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_search.*
import me.dynerowicz.wtest.presenter.PostalCodeRow
import me.dynerowicz.wtest.presenter.PostalCodeRowAdapter
import me.dynerowicz.wtest.tasks.DatabaseQueryListener
import me.dynerowicz.wtest.tasks.DatabaseQueryTask

class SearchFragment : Fragment(), View.OnClickListener, DatabaseQueryListener {

    private var recyclerViewAdapter = PostalCodeRowAdapter()

    var dbHelper: SQLiteOpenHelper? = null
    private var searchInProgress: ProgressDialog? = null

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
        val localDbHelper = dbHelper
        localDbHelper?.let {
            searchInProgress = ProgressDialog.show(
                    context,
                    context?.getString(R.string.SearchInProgress),
                    context?.getString(R.string.PleaseWait),
                    true)

            val splitInput = searchInput.split(" ").toTypedArray()

            //TODO: prevent SQL injection
            DatabaseQueryTask(localDbHelper, this).execute(*splitInput)
        }
    }

    override fun onQueryComplete(results: List<PostalCodeRow>) {
        searchInProgress?.dismiss()
        Log.v(TAG, "onQueryComplete")
        Log.v(TAG, "Found ${results.size} matching postal codes entries")
        Toast.makeText(context, "Found ${results.size} matching postal codes entries", Toast.LENGTH_LONG).show()
        recyclerViewAdapter.postalCodeRowsCursor = results
        recyclerViewAdapter.notifyDataSetChanged()
    }

    override fun onQueryCancelled() {
        searchInProgress?.dismiss()
    }

    companion object {
        const val TAG = "SearchFragment"
    }
}