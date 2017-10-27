package me.dynerowicz.wtest.tasks

import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.util.Log
import me.dynerowicz.wtest.database.DatabaseContract
import me.dynerowicz.wtest.presenter.PostalCodeRow
import me.dynerowicz.wtest.utils.parseCsvLine
import java.io.File
import java.io.IOException

// TODO: take into account the encoding of the CSV file ...
class CsvImporterTask(
        private val database: SQLiteDatabase,
        private val csvFile: File,
        private val importListener: CsvImportListener?
) : AsyncTask<Unit, Int, Pair<Long, Long>>() {
    private var localityIdentifiers = HashMap<String, Long>()

    private fun determineEntryCount(csvFile: File): Long {
        val reader = csvFile.bufferedReader()

        var numberOfLines = 0L
        try {
            while (reader.readLine() != null) {
                numberOfLines += 1L
            }
        } catch (ioe: IOException) {
            Log.e(TAG, "Error while pre-processing CSV file: $ioe")
            numberOfLines = -1L
        } finally {
            reader.close()
        }

        return numberOfLines
    }

    override fun onPostExecute(result: Pair<Long, Long>) {
        super.onPostExecute(result)
        importListener?.onImportComplete(result)
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        if(values.isNotEmpty()) {
            val progressUpdate = values.first()
            if(progressUpdate != null)
                importListener?.onImportProgressUpdate(progressUpdate)
        }
    }

    override fun onCancelled() {
        importListener?.onImportCancelled()
    }

    override fun doInBackground(vararg p0: Unit?): Pair<Long, Long> {
        val csvFileReader = csvFile.bufferedReader()

        val totalNumberOfEntries = determineEntryCount(csvFile)

        var currentProgress = 0
        var importedEntriesCount = 0L
        var invalidEntriesCount = 0L

        var lineNumber = 0
        val fields = Array(3) { StringBuilder() }

        if (csvFileReader.ready()) {
            val fieldNameCount = fields.parseCsvLine(csvFileReader.readLine(), 0, 11, 12)
            lineNumber += 1

            if(fieldNameCount == DatabaseContract.CSV_NUMBER_OF_FIELDS) {
                val field0 = fields[0].toString()
                val field11 = fields[1].toString()
                val field12 = fields[2].toString()

                Log.v(TAG, "Found $field0, $field11, $field12")
                if (field0 == DatabaseContract.CSV_LOCALITY && field11 == DatabaseContract.CSV_POSTAL_CODE && field12 == DatabaseContract.CSV_EXTENSION) {
                    publishProgress(0)

                    database.beginTransaction()

                    while (csvFileReader.ready() && !isCancelled) {
                        val fieldCount = fields.parseCsvLine(csvFileReader.readLine(), 0, 11, 12)
                        lineNumber += 1

                        if (fieldCount == DatabaseContract.CSV_NUMBER_OF_FIELDS) {
                            try {
                                val locality = fields[0].toString()
                                val postalCode = fields[1].toString().toLong()
                                val extension = fields[2].toString().toLong()
                                insertPostalCode(postalCode, extension, locality)
                            } catch (nfe: NumberFormatException) {
                                Log.v(TAG, "Error at line $lineNumber: $nfe")
                            }
                            importedEntriesCount += 1

                            if (importedEntriesCount.rem(10000) == 0L) {
                                database.setTransactionSuccessful()
                                database.endTransaction()
                                database.beginTransaction()
                            }

                            val progressUpdate = (importedEntriesCount * 100L / totalNumberOfEntries).toInt()

                            if (currentProgress != progressUpdate) {
                                Log.v(TAG, "ImportProgressUpdate: $importedEntriesCount / $totalNumberOfEntries entries [$progressUpdate%] (invalid=$invalidEntriesCount)")
                                currentProgress = progressUpdate
                                publishProgress(currentProgress)
                            }

                        } else {
                            Log.e(TAG, "Ill-formed CSV file@line $lineNumber]: $fields")
                            invalidEntriesCount += 1
                        }
                    }

                    database.setTransactionSuccessful()
                    database.endTransaction()
                }
            } else
                Log.e(TAG, "Invalid CSV Header: $fields")

            if (invalidEntriesCount > 0)
                Log.e(TAG, "Invalid entries found in CSV files: $invalidEntriesCount")
        }

        csvFileReader.close()

        database.execSQL(DatabaseContract.CREATE_POSTAL_CODES_INDEX)
        database.execSQL(DatabaseContract.CREATE_LOCALITY_ID_INDEX)

        return Pair(importedEntriesCount, invalidEntriesCount)
    }

    // Database insertion operation
    private val insertIntoLocalityNames = database.compileStatement(DatabaseContract.INSERT_INTO_LOCALITY_NAMES_STATEMENT)
    private val insertIntoPostalCodes = database.compileStatement(DatabaseContract.INSERT_INTO_POSTAL_CODES_STATEMENT)
    private fun insertPostalCode(postalCode: Long, extension: Long, locality: String) {
        val localityIdentifier: Long = localityIdentifiers[locality] ?:
            insertIntoLocalityNames.run {
                bindString(1, locality)
                val assignedIdentifier = executeInsert()
                localityIdentifiers.put(locality, assignedIdentifier)
                assignedIdentifier
            }

        insertIntoPostalCodes.run {
            clearBindings()
            bindLong(1, PostalCodeRow.postalCodeWithExtension(postalCode, extension))
            bindLong(2, localityIdentifier)
            executeInsert()
            clearBindings()
        }
    }

    companion object {
        const val TAG = "CsvImporterTask"
    }
}