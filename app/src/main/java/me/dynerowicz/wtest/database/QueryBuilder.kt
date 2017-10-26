package me.dynerowicz.wtest.database

class QueryBuilder {
    var select: Array<out String>? = null
    var fromTable: String? = null

    var matchPostalCodeWithExtensionMinimum: Long? = null
    var matchPostalCodeWithExtensionMaximum: Long? = null

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

                if (matchPostalCodeWithExtensionMinimum != null || matchPostalCodeWithExtensionMaximum != null || matchLocalityKeywords.isNotEmpty()) {
                    append(" WHERE ")

                    if (matchPostalCodeWithExtensionMinimum != null || matchPostalCodeWithExtensionMaximum != null) {
                        appendPostalCodeConstraint(matchPostalCodeWithExtensionMinimum, matchPostalCodeWithExtensionMaximum)

                        if (matchLocalityKeywords.isNotEmpty())
                            append(" AND ")
                    }

                    matchLocalityKeywords.forEachIndexed { index, keyword ->
                        appendLocalityConstraint(keyword)
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

//TODO: this is awful ...
private fun expandedKeywords(expanded: MutableList<String>, keyword: String, index: Int = 0, partialKeyword: StringBuilder = StringBuilder()) {
    if (index == keyword.length)
        expanded.add(partialKeyword.toString())
    else {
        val character = keyword[index]
        val cases: String =
            when (character) {
                'a', 'â', 'ã', 'à' -> "aâãà"
                'e', 'é', 'ê'      -> "eéê"
                'i', 'í'           -> "ií"
                'o', 'ó', 'ô', 'õ' -> "oóôõ"
                'u', 'ú'           -> "uú"
                else               -> character.toString()
            }
        cases.forEach { characterAlternative ->
            partialKeyword.append(characterAlternative)
            expandedKeywords(expanded, keyword, index + 1, partialKeyword)
            partialKeyword.deleteCharAt(index)
        }
    }
}

private fun StringBuilder.appendLocalityConstraint(keyword: String) {
    val expandedKeywords = ArrayList<String>()
    expandedKeywords(expandedKeywords, keyword.toLowerCase())

    expandedKeywords.forEachIndexed { index, expandedKeyword ->
        append("(${DatabaseContract.COLUMN_LOCALITY} LIKE '%$expandedKeyword%' OR ")
        append("${DatabaseContract.COLUMN_LOCALITY} LIKE '%${expandedKeyword.capitalize()}%')")
        if (index < expandedKeywords.size - 1)
            append(" OR ")
    }
}

private fun StringBuilder.appendPostalCodeConstraint(minimum: Long?, maximum: Long?) {
    if (minimum != null)
        append("$minimum <= ${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}")

    if (minimum != null && maximum != null)
        append(" AND ")

    if (maximum != null)
        append("${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION} <= $maximum")
}
