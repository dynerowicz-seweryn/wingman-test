package me.dynerowicz.wtest.tasks

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.AsyncTask
import android.util.Log
import me.dynerowicz.wtest.database.DatabaseContract
import me.dynerowicz.wtest.presenter.PostalCodeRow
import me.dynerowicz.wtest.database.QueryBuilder

class DatabaseQueryTask(
    private val dbHelper: SQLiteOpenHelper,
    private val queryListener: DatabaseQueryListener? = null
) : AsyncTask<String, Unit, Cursor>() {

    override fun onPostExecute(result: Cursor) {
        super.onPostExecute(result)
        Log.v("DatabaseQueryTask", "onPostExecute : ${result.count} rows found")
        queryListener?.onQueryComplete(result)
    }

    override fun onCancelled() {
        Log.v("DatabaseQueryTask", "onCancelled")
        queryListener?.onQueryCancelled()
    }

    private fun constructQuery(inputs: Array<out String?>): String {
        val queryBuilder = getBaseQuery()

        var postalCodeFound = false
        var extensionFound = false

        Log.v(TAG, "Processing <$inputs>")
        inputs.forEach { input ->
            if (input != null) {
                var inputConsumed = false
                try {
                    val parsedLong = input.toLong()
                    //TODO: case where the parsed long is a partial postal code: '37' should result in searching for postal codes between '3700' and '3800'
                    // if we already found a Postal Code and an Extension, consume this input but ignore it
                    if (!postalCodeFound) {
                        queryBuilder.matchPostalCode = parsedLong
                        postalCodeFound = true
                    }
                    else if (!extensionFound) {
                        queryBuilder.matchExtension = parsedLong
                        extensionFound = true
                    }

                    inputConsumed = true

                } catch (nfe: NumberFormatException) {
                    // If it was not possible to parse to a Long, the if after will take care of this case
                }

                if (!inputConsumed)
                    queryBuilder.addLocalityKeyword(input)
            }
        }

        return queryBuilder.build()
    }

    override fun doInBackground(vararg inputs: String?): Cursor {
        val database = dbHelper.readableDatabase
        val query = constructQuery(inputs)
        Log.v(TAG, "Executing query : $query")
        return database.rawQuery(query, null)
    }

    companion object {
        const val TAG = "DatabaseQueryTask"

        fun getBaseQuery() = QueryBuilder().apply {
            select = arrayOf(DatabaseContract.COLUMN_POSTAL_CODE,
                             DatabaseContract.COLUMN_EXTENSION,
                             DatabaseContract.COLUMN_LOCALITY)
            fromTable = DatabaseContract.TABLE_NAME
            orderBy = arrayOf(DatabaseContract.COLUMN_POSTAL_CODE, DatabaseContract.COLUMN_EXTENSION)
        }
    }
}

fun Cursor.getPostalCodeRow() : PostalCodeRow = PostalCodeRow(getLong(0), getLong(1), getString(2))