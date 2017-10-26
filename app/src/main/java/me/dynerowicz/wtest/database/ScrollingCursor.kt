package me.dynerowicz.wtest.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import me.dynerowicz.wtest.presenter.PostalCodeRow
import me.dynerowicz.wtest.tasks.DatabaseQueryListener
import me.dynerowicz.wtest.tasks.DatabaseQueryTask
import me.dynerowicz.wtest.tasks.getPostalCodeRow

class ScrollingCursor(
        private val database: SQLiteDatabase,
        private val listener: ScrollingCursorObserver,
        val windowSize: Int = 100,
        vararg inputs: String
) : DatabaseQueryListener {

    private var queryBuilder = QueryBuilder(
        select = arrayOf(DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION,
                DatabaseContract.COLUMN_LOCALITY),
        fromTable = DatabaseContract.TABLE_NAME,
        orderBy = arrayOf(DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION),
        limitTo = windowSize,
        inputs = *inputs
    )

    private var currentFinal: PostalCodeRow? = null

    private val postalCodeRows: MutableList<PostalCodeRow> = ArrayList()
    private var count: Int = 0

    fun initialize() {
        DatabaseQueryTask(database, this).execute(queryBuilder)
    }

    // Assumption 0 <= position < count
    fun getPostalCodeRow(position: Int): PostalCodeRow {
        if (position == count - 50) {
            queryBuilder.starting = currentFinal
            DatabaseQueryTask(database, this).execute(queryBuilder)
        }

        return postalCodeRows[position]
    }

    fun count() = count

    override fun onQueryComplete(results: Cursor) {
        super.onQueryComplete(results)

        while(results.moveToNext())
            postalCodeRows.add(results.getPostalCodeRow())

        currentFinal = postalCodeRows.last()
        count += results.count

        if (results.count > 0)
            listener.onMoreResultsAvailable(results.count)
        else
            listener.onEndOfResults()
    }

    companion object {
        val tag = ScrollingCursor::class.java.simpleName
    }
}