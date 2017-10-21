package me.dynerowicz.wtest.tasks

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.util.Log

class DatabaseQueryTask(
    private val database: SQLiteDatabase,
    private val queryListener: DatabaseQueryListener? = null
) : AsyncTask<String, Unit, Cursor?>() {

    override fun onPostExecute(result: Cursor?) {
        super.onPostExecute(result)
        Log.v("DatabaseQueryTask", "onPostExecute")
        queryListener?.onQueryComplete(result)
    }

    override fun onCancelled() {
        Log.v("DatabaseQueryTask", "onCancelled")
        queryListener?.onQueryCancelled()
    }

    override fun doInBackground(vararg queries: String?): Cursor? {
        var cursor: Cursor? = null

        if (queries.isNotEmpty()) {
            val query = queries.first()
            query?.let {
                cursor = database.rawQuery(query, null)
            }
        }

        return cursor
    }

    companion object {
        const val TAG = "DatabaseQueryTask"
    }
}