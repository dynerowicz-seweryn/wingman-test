package me.dynerowicz.wtest.tasks

import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.util.Log
import me.dynerowicz.wtest.presenter.PostalCodeRow

class DatabaseQueryTask(
    private val database: SQLiteDatabase,
    private val queryListener: DatabaseQueryListener? = null
) : AsyncTask<String, Unit, List<PostalCodeRow>>() {

    override fun onPostExecute(result: List<PostalCodeRow>) {
        super.onPostExecute(result)
        Log.v("DatabaseQueryTask", "onPostExecute : ${result.size} rows found")
        queryListener?.onQueryComplete(result)
    }

    override fun onCancelled() {
        Log.v("DatabaseQueryTask", "onCancelled")
        queryListener?.onQueryCancelled()
    }

    override fun doInBackground(vararg queries: String?): List<PostalCodeRow>? {
        val list = ArrayList<PostalCodeRow>()

        if (queries.isNotEmpty()) {
            val query = queries.first()
            query?.let {
                val cursor = database.rawQuery(query, null)
                cursor?.run {
                    moveToFirst()

                    while (!isAfterLast) {
                        list.add(PostalCodeRow(getLong(1), getLong(2), getString(3)))
                        moveToNext()
                    }
                }
            }
        }

        return list
    }

    companion object {
        const val TAG = "DatabaseQueryTask"
    }
}