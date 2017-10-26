package me.dynerowicz.wtest.tasks

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.util.Log
import me.dynerowicz.wtest.database.QueryBuilder
import me.dynerowicz.wtest.presenter.PostalCodeRow

class DatabaseQueryTask(
    private val database: SQLiteDatabase,
    private val queryListener: DatabaseQueryListener? = null
) : AsyncTask<QueryBuilder, Unit, Cursor>() {

    override fun doInBackground(vararg queries: QueryBuilder): Cursor {
        if (queries.isEmpty())
            throw IllegalArgumentException("DatabaseQueryTask requires a query string")

        val query = queries.first().toString()
        Log.v(TAG, "Query: $query")

        return database.rawQuery(query, null)
    }

    override fun onPostExecute(results: Cursor) {
        super.onPostExecute(results)
        Log.v("DatabaseQueryTask", "onPostExecute : ${results.count} rows found")
        queryListener?.onQueryComplete(results)
    }

    override fun onCancelled() {
        Log.v("DatabaseQueryTask", "onCancelled")
        queryListener?.onQueryCancelled()
    }

    companion object {
        const val TAG = "DatabaseQueryTask"
    }
}

fun Cursor.getPostalCodeRow() : PostalCodeRow = PostalCodeRow(getLong(0), getString(1))