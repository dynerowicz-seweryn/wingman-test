package me.dynerowicz.wtest.tasks

import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.util.Log
import me.dynerowicz.wtest.database.DatabaseContract
import me.dynerowicz.wtest.presenter.PostalCodeRow
import me.dynerowicz.wtest.database.QueryBuilder

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

    private fun constructQuery(inputs: Array<out String?>): String {
        val queryBuilder = QueryBuilder().apply {
            select = arrayOf(DatabaseContract.COLUMN_POSTAL_CODE,
                             DatabaseContract.COLUMN_EXTENSION,
                             DatabaseContract.COLUMN_LOCALITY)
            fromTable = DatabaseContract.TABLE_NAME
            orderBy = arrayOf(DatabaseContract.COLUMN_POSTAL_CODE, DatabaseContract.COLUMN_EXTENSION)
        }

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

    override fun doInBackground(vararg inputs: String?): List<PostalCodeRow>? {
        val list = ArrayList<PostalCodeRow>()

        val query = constructQuery(inputs)
        Log.v(TAG, "Query : $query")

        val cursor = database.rawQuery(query, null)
        cursor?.run {
            moveToFirst()

            while (!isAfterLast) {
                list.add(PostalCodeRow(getLong(0), getLong(1), getString(2)))
                moveToNext()
            }
        }
        cursor.close()

        return list
    }

    companion object {
        const val TAG = "DatabaseQueryTask"
    }
}