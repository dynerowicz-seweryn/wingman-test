package me.dynerowicz.wtest.database

import android.util.Log
import me.dynerowicz.wtest.presenter.PostalCodeRow
import org.apache.commons.lang3.StringUtils

class QueryBuilder(
    val limitTo: Int = -1,
    vararg inputs: String
) {
    private var postalCodeWithExtensionMinimum: Long? = null
    private var postalCodeWithExtensionMaximum: Long? = null
    private var localityKeywords = ArrayList<String>()

    var startingPostalCodeRow: PostalCodeRow? = null

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
        stringBuilder.append("SELECT DISTINCT PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}, LN.${DatabaseContract.COLUMN_NAME}")

        if (localityKeywords.isNotEmpty()) {
            stringBuilder.append(" FROM ${DatabaseContract.LOCALITY_NORMALIZED_NAMES_TABLE} NN")
            stringBuilder.append(" LEFT JOIN ${DatabaseContract.LOCALITIES_TABLE} LN")
            stringBuilder.append(" ON NN.${DatabaseContract.COLUMN_ID} == LN.${DatabaseContract.COLUMN_NORMALIZED_NAME_IDENTIFIER}")
            stringBuilder.append(" LEFT JOIN ${DatabaseContract.POSTAL_CODES_TABLE} PC")
            stringBuilder.append(" ON LN.${DatabaseContract.COLUMN_ID} == PC.${DatabaseContract.COLUMN_LOCALITY_IDENTIFIER}")
        } else {
            stringBuilder.append(" FROM ${DatabaseContract.POSTAL_CODES_TABLE} PC")
            stringBuilder.append(" LEFT JOIN ${DatabaseContract.LOCALITIES_TABLE} LN")
            stringBuilder.append(" ON LN.${DatabaseContract.COLUMN_ID} == PC.${DatabaseContract.COLUMN_LOCALITY_IDENTIFIER}")
        }

        if(localityKeywords.isNotEmpty() || startingPostalCodeRow != null || postalCodeWithExtensionMinimum != null || postalCodeWithExtensionMaximum != null) {
            stringBuilder.append(" WHERE ")

            // First the locality keywords to match against the Normalized Names table
            if (localityKeywords.isNotEmpty()) {
                localityKeywords.forEachIndexed { index, keyword ->
                    stringBuilder.appendLocalityConstraint(keyword)
                    if (index < localityKeywords.size - 1)
                        stringBuilder.append(" AND ")
                }
                if (startingPostalCodeRow != null || postalCodeWithExtensionMinimum != null || postalCodeWithExtensionMaximum != null)
                    stringBuilder.append(" AND ")
            }

            // Constructing the remainder of the WHERE clause
            val starting = startingPostalCodeRow
            if (starting != null) {
                stringBuilder.appendStartingConstraint(starting, postalCodeWithExtensionMaximum)
            } else if (postalCodeWithExtensionMinimum != null || postalCodeWithExtensionMaximum != null) {
                stringBuilder.appendPostalCodeConstraint(postalCodeWithExtensionMinimum, postalCodeWithExtensionMaximum)
            }
        }

        // Constructing the ORDER BY clause
        stringBuilder.append(" ORDER BY "
                + "PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}"
                + ","
                + "LN.${DatabaseContract.COLUMN_NAME}"
        )

        // Constructing the LIMIT clause
        if (limitTo > 0)
            stringBuilder.append(" LIMIT $limitTo")

        return stringBuilder.toString()
    }

    companion object {
        val TAG = QueryBuilder::class.java.simpleName
    }
}

private fun StringBuilder.appendPostalCodeConstraint(minimum: Long?, maximum: Long?) {
    if (minimum != null && maximum != null)
        append("(PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION} BETWEEN $minimum AND $maximum)")
    else {
        if (minimum != null)
            append("$minimum <= PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}")
        else
            append("PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION} <= $maximum")
    }
}

private fun StringBuilder.appendStartingConstraint(postalCodeRow: PostalCodeRow, maximum: Long?) {
    val pcMinimum = postalCodeRow.postalCodeWithExtension
    append("(($pcMinimum <  PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}) OR")
    append(" ($pcMinimum  == PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION} AND")
    append(" '${postalCodeRow.locality}' < LN.${DatabaseContract.COLUMN_NAME}))")
    if (maximum != null)
        append(" AND (PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION} <= $maximum)")
}

private fun StringBuilder.appendLocalityConstraint(keyword: String) {
    val sanitizedKeyword = StringUtils.stripAccents(keyword.decapitalize())
    append("(NN.${DatabaseContract.COLUMN_NAME} LIKE '%$sanitizedKeyword%' OR ")
    append(" NN.${DatabaseContract.COLUMN_NAME} LIKE '%${sanitizedKeyword.capitalize()}%')")
}