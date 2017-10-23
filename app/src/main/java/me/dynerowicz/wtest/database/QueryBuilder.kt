package me.dynerowicz.wtest.database

import org.apache.commons.lang3.StringUtils

class QueryBuilder {
    var select: Array<out String>? = null
    var fromTable: String? = null
    var matchPostalCode: Long? = null
    var matchExtension: Long? = null
    private var matchLocalityKeywords = ArrayList<String>()
    var orderBy: Array<out String>? = null

    fun addLocalityKeyword(keyword: String): QueryBuilder {
        matchLocalityKeywords.add(keyword)
        return this
    }

    fun build(): String {
        val columns = select
        val table = fromTable

        if (columns == null || table == null)
            throw IllegalArgumentException("Selected columns and Table name must be initialized. Call methods select() and from().")
        else {
            return StringBuilder().apply {
                append("SELECT DISTINCT ")

                if (columns.isEmpty())
                    append("*")
                else columns.forEachIndexed { index, column ->
                    append(column)
                    if (index < columns.size - 1)
                        append(',')
                }

                append(" FROM $table")

                if (matchPostalCode != null || matchExtension != null || matchLocalityKeywords.isNotEmpty()) {
                    append(" WHERE ")

                    matchPostalCode?.let {
                        append("${DatabaseContract.COLUMN_POSTAL_CODE} == $matchPostalCode")
                        if (matchExtension != null || matchLocalityKeywords.isNotEmpty())
                            append(" AND ")
                    }

                    matchExtension?.let {
                        append("${DatabaseContract.COLUMN_EXTENSION} == $matchExtension")
                        if (matchLocalityKeywords.isNotEmpty())
                            append(" AND ")
                    }

                    matchLocalityKeywords.forEachIndexed { index, keyword ->
                        append("${DatabaseContract.COLUMN_LOCALITY_NORMALIZED} LIKE '%${StringUtils.stripAccents(keyword)}%'")
                        if (index < matchLocalityKeywords.size - 1)
                            append(" AND ")
                    }
                }

                val order = orderBy
                if (order != null && order.isNotEmpty()) {
                    append(" ORDER BY ")
                    order.forEachIndexed { index, column ->
                        append(column)
                        if (index < order.size - 1)
                            append(',')
                    }
                }

            }.toString()
        }
    }

    companion object {
        val TAG = QueryBuilder::class.java.simpleName
    }
}