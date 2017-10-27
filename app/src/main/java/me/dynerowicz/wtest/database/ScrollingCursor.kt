package me.dynerowicz.wtest.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import me.dynerowicz.wtest.presenter.PostalCodeRow
import me.dynerowicz.wtest.tasks.DatabaseQueryListener
import me.dynerowicz.wtest.tasks.DatabaseQueryTask

class ScrollingCursor(
        private val database: SQLiteDatabase,
        private val listener: ScrollingCursorObserver,
        private val windowSize: Int = 30,
        private val threshold: Int = windowSize
) : DatabaseQueryListener {

    var inputs: Array<out String>? = null
        set(value) {
            value?.let {
                queryBuilder = QueryBuilder(
                    limitTo = windowSize,
                    inputs = *value
                )
            }
        }

    private lateinit var queryBuilder: QueryBuilder

    private var currentFinal: PostalCodeRow? = null
    private var endOfResultsReached = false

    private val postalCodeRows: MutableList<PostalCodeRow> = ArrayList()
    private var count: Int = 0

    fun start() {
        DatabaseQueryTask(database, this, appendTo = postalCodeRows).execute(queryBuilder)
    }

    // Assumption 0 <= position < count
    fun getPostalCodeRow(position: Int): PostalCodeRow {
        if (!endOfResultsReached && (position == count - threshold)) {
            Log.v(tag, "Threshold passed: initiating next query")
            queryBuilder.starting = currentFinal
            DatabaseQueryTask(database, this, appendTo = postalCodeRows).execute(queryBuilder)
        }

        return postalCodeRows[position]
    }

    fun count() = count

    override fun onQueryComplete(results: Cursor) {
        super.onQueryComplete(results)

        currentFinal = postalCodeRows.last()
        val oldCount = count
        count += results.count

        if (results.count < windowSize) {
            endOfResultsReached = true
            listener.onEndOfResults()
        }

        if (results.count > 0)
            listener.onMoreResultsAvailable(oldCount, results.count)

        results.close()
    }

    companion object {
        val tag = ScrollingCursor::class.java.simpleName
    }
}