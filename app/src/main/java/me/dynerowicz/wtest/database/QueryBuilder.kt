package me.dynerowicz.wtest.database

import android.util.Log
import me.dynerowicz.wtest.presenter.PostalCodeRow

class QueryBuilder(
    val limitTo: Int = -1,
    vararg inputs: String
) {
    private var postalCodeWithExtensionMinimum: Long? = null
    private var postalCodeWithExtensionMaximum: Long? = null
    private var localityKeywords = ArrayList<String>()

    var starting: PostalCodeRow? = null

    init {
        var postalCodeFound = false

        Log.v(TAG, "Processing <$inputs>")
        inputs.forEach { input ->
            var inputConsumed = false
            try {
                val parsedLong = input.toLong()
                //TODO: case where the parsed long is a partial postal code: '37' should result in searching for postal codes between '3700' and '3800'
                // if we already found a Postal Code and an Extension, consume this input but ignore it
                if (!postalCodeFound) {
                    postalCodeWithExtensionMinimum = parsedLong * Math.pow(10.0, 7.0 - input.length).toLong()
                    postalCodeWithExtensionMaximum = (parsedLong * Math.pow(10.0, 7.0 - input.length) + (Math.pow(10.0, 7.0 - input.length) - 1.0)).toLong()
                    postalCodeFound = true
                }

                inputConsumed = true

            } catch (nfe: NumberFormatException) {
                // If it was not possible to parse to a Long, the if after will take care of this case
            }

            if (!inputConsumed)
                localityKeywords.add(input)
        }
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        // Constructing the projection
        stringBuilder.append("SELECT DISTINCT PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}, LN.${DatabaseContract.COLUMN_LOCALITY_NAME}")
        stringBuilder.append(" FROM ${DatabaseContract.LOCALITY_NAMES_TABLE} LN")
        stringBuilder.append(" INNER JOIN ${DatabaseContract.POSTAL_CODES_TABLE} PC")
        stringBuilder.append(" ON PC.${DatabaseContract.COLUMN_LOCALITY_IDENTIFIER} == LN.${DatabaseContract.COLUMN_LOCALITY_NAME_ID}")

        if (starting != null || postalCodeWithExtensionMinimum != null || postalCodeWithExtensionMaximum != null || localityKeywords.isNotEmpty()) {
            stringBuilder.append(" WHERE ")

            // Constructing the remainder of the WHERE clause
            if (starting != null) {
                stringBuilder.appendStarting(starting)
                if (postalCodeWithExtensionMinimum != null || postalCodeWithExtensionMaximum != null || localityKeywords.isNotEmpty())
                stringBuilder.append(" AND ")
            }

            if (postalCodeWithExtensionMinimum != null || postalCodeWithExtensionMaximum != null) {
                stringBuilder.appendPostalCodeConstraint(postalCodeWithExtensionMinimum, postalCodeWithExtensionMaximum)
                if (localityKeywords.isNotEmpty())
                    stringBuilder.append(" AND ")
            }

            localityKeywords.forEachIndexed { index, keyword ->
                stringBuilder.appendLocalityConstraint(keyword)
                if (index < localityKeywords.size - 1)
                    stringBuilder.append(" AND ")
            }
        }

        // Constructing the ORDER BY clause
        stringBuilder.append(" ORDER BY LN.${DatabaseContract.COLUMN_LOCALITY_NAME}, PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}")

        // Constructing the LIMIT clause
        if (limitTo > 0)
            stringBuilder.append(" LIMIT $limitTo")

        return stringBuilder.toString()
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
                'a'  -> "aâãà"
                'e'  -> "eéê"
                'i'  -> "ií"
                'o'  -> "oóôõ"
                'u'  -> "uú"
                else -> character.toString()
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

    append('(')
    expandedKeywords.forEachIndexed { index, expandedKeyword ->
        append("(LN.${DatabaseContract.COLUMN_LOCALITY_NAME} LIKE '%$expandedKeyword%' OR ")
        append(" LN.${DatabaseContract.COLUMN_LOCALITY_NAME} LIKE '%${expandedKeyword.capitalize()}%')")
        if (index < expandedKeywords.size - 1)
            append(" OR ")
    }
    append(')')
}

private fun StringBuilder.appendStarting(pcRow: PostalCodeRow?) {
    if(pcRow != null) {
        append("(${pcRow.postalCodeWithExtension} <= PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION} AND ")
        append("'${pcRow.locality}' <= LN.${DatabaseContract.COLUMN_LOCALITY_NAME})")
    }
}

private fun StringBuilder.appendPostalCodeConstraint(minimum: Long? = null, maximum: Long? = null) {
    if (minimum != null)
        append("$minimum <= PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}")

    if (minimum != null && maximum != null)
        append(" AND ")

    if (maximum != null)
        append("PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION} <= $maximum")
}
