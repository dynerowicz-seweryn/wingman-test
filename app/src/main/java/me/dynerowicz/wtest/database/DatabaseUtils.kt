package me.dynerowicz.wtest.database

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.util.Log
import com.opencsv.CSVReader
import java.io.File
import java.io.IOException

const val TAG = "DatabaseUtils"

interface ImportProgressListener {
    fun onImportProgressUpdate(new: Int)
    fun onImportComplete(result: Pair<Long, Long>)
}

private fun determineEntryCount(csvFile: File): Long {
    val reader = csvFile.bufferedReader()

    var numberOfLines = 0L
    try {
        while (reader.readLine() != null) {
            numberOfLines += 1L
        }
    } catch (ioe: IOException) {
        Log.e(TAG, "Error while pre-processing CSV file: $ioe")
    }

    reader.close()

    return numberOfLines
}

const val TRANSACTION_SIZE = 5000

fun SQLiteDatabase.importFromCsv(csvFile: File, listener: ImportProgressListener): Pair<Long, Long> {
    val insertStatement: SQLiteStatement = this.compileStatement(DatabaseContract.INSERT_QUERY)

    val csvFileReader = CSVReader(csvFile.bufferedReader())
    val parserIterator = csvFileReader.iterator()

    val totalNumberOfEntries = determineEntryCount(csvFile)

    var currentProgress = 0
    var importedEntriesCount = 0L
    var invalidEntriesCount = 0L

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
                listener.onImportProgressUpdate(0)

                beginTransaction()

                while (parserIterator.hasNext()) {
                    fields = parserIterator.next()
                    lineNumber += 1

                    if (fields.size == fieldNames.size) {
                        try {
                            val locality = fields[localityIndex]
                            val postalCode = fields[postalCodeIndex].toLong()
                            val extension = fields[extensionIndex].toLong()

                            insertStatement.execute(postalCode, extension, locality)

                        } catch (nfe: NumberFormatException) {
                            Log.v(TAG, "Error at line $lineNumber: $nfe")
                        }
                        importedEntriesCount += 1

                        if (importedEntriesCount.rem(TRANSACTION_SIZE) == 0L) {
                            setTransactionSuccessful()
                            endTransaction()
                            beginTransaction()
                        }

                        val progressUpdate = (importedEntriesCount * 100L / totalNumberOfEntries).toInt()

                        if (progressUpdate != currentProgress) {
                            Log.v(TAG, "ImportProgressUpdate: $importedEntriesCount / $totalNumberOfEntries entries [$progressUpdate%] (invalid=$invalidEntriesCount)")
                            currentProgress = progressUpdate
                            listener.onImportProgressUpdate(progressUpdate)
                        }

                    } else {
                        Log.e(TAG, "Ill-formed CSV file@line $lineNumber]: $fieldNames")
                        invalidEntriesCount += 1
                    }
                }

                setTransactionSuccessful()
                endTransaction()
            }
        } else
            Log.e(TAG, "Invalid CSV Header: $fieldNames")

        if (invalidEntriesCount > 0)
            Log.e(TAG, "Invalid entries found in CSV files: $invalidEntriesCount")
    }

    listener.onImportComplete(Pair(importedEntriesCount, invalidEntriesCount))
    csvFileReader.close()

    return Pair(importedEntriesCount, invalidEntriesCount)
}