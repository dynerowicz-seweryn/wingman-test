package me.dynerowicz.wtest.database

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.opencsv.CSVReader
import java.io.File

const val TAG = "DatabaseUtils"

fun SQLiteDatabase.importFromCsv(csvFile: File): Pair<Int, Int> {
    val csvFileReader = CSVReader(csvFile.bufferedReader())
    val parserIterator = csvFileReader.iterator()

    var importedEntriesCount = 0
    var invalidEntriesCount = 0

    var lineNumber = 0
    val fieldNames: Array<String>
    if (parserIterator.hasNext()) {
        fieldNames = parserIterator.next()
        lineNumber += 1

        var validCsvHeader = fieldNames.size == DatabaseContract.CSV_NUMBER_OF_FIELDS

        var fields: Array<String>

        if(validCsvHeader) {
            val localityIndex = fieldNames.indexOf(DatabaseContract.CSV_LOCALITY)
            val postalCodeIndex = fieldNames.indexOf(DatabaseContract.CSV_POSTAL_CODE)
            val extensionIndex = fieldNames.indexOf(DatabaseContract.CSV_EXTENSION)

            validCsvHeader = localityIndex != -1 && postalCodeIndex != -1 && extensionIndex != -1

            if (validCsvHeader) {
                while (parserIterator.hasNext()) {
                    fields = parserIterator.next()
                    lineNumber += 1

                    if (fields.size == fieldNames.size) {
                        try {
                            val locality = fields[localityIndex]
                            val postalCode = fields[postalCodeIndex].toInt()
                            val extension = fields[extensionIndex].toInt()

                            val rowId = insertPostalCode(postalCode, extension, locality)
                            Log.d(TAG, "Insert: $rowId")
                        } catch (nfe: NumberFormatException) {
                            Log.v(TAG, "Error at line $lineNumber: $nfe")
                        }
                        importedEntriesCount += 1
                    } else {
                        Log.e(TAG, "Ill-formed CSV file@line $lineNumber]: $fieldNames")
                        invalidEntriesCount += 1
                    }
                }
            }
        } else
            Log.e(TAG, "Invalid CSV Header: $fieldNames")

        if (invalidEntriesCount > 0)
            Log.e(TAG, "Invalid entries found in CSV files: $invalidEntriesCount")
    }

    csvFileReader.close()

    return Pair(importedEntriesCount, invalidEntriesCount)
}