package me.dynerowicz.wtest.tasks

import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.util.Log
import me.dynerowicz.wtest.database.DatabaseContract
import me.dynerowicz.wtest.utils.parseCsvLine
import java.io.File

class CsvImportTask(
        private val database: SQLiteDatabase,
        private val csvEntriesFile: File,
        private val identifier: Int = 0,
        private val importListener: CsvImportListener? = null
) : AsyncTask<Unit, Long, Pair<Long, Long>>() {

    override fun onPostExecute(result: Pair<Long, Long>) {
        super.onPostExecute(result)
        importListener?.onImportComplete(result)
    }

    override fun onProgressUpdate(vararg values: Long?) {
        super.onProgressUpdate(*values)
        if(values.isNotEmpty()) {
            val progressUpdate = values.first()
            if(progressUpdate != null)
                importListener?.onImportUpdate(Pair(identifier, progressUpdate))
        }
    }

    override fun onCancelled() {
        importListener?.onImportCancelled()
    }

    override fun doInBackground(vararg p0: Unit?): Pair<Long, Long> {
        val csvFileReader = csvEntriesFile.bufferedReader()

        var importedEntriesCount = 0L
        var invalidEntriesCount = 0L

        var lineNumber = 0
        val fields = Array(3) { StringBuilder() }

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

                if (importedEntriesCount.rem(TRANSACTION_SIZE) == 0L) {
                    database.setTransactionSuccessful()
                    database.endTransaction()
                    publishProgress(importedEntriesCount)
                    database.beginTransaction()
                }

            } else {
                Log.e(TAG, "Ill-formed CSV file@line $lineNumber]: $fields")
                invalidEntriesCount += 1
            }
        }

        database.setTransactionSuccessful()
        database.endTransaction()

        if (invalidEntriesCount > 0)
            Log.e(TAG, "Invalid entries found in CSV files: $invalidEntriesCount")

        csvFileReader.close()

        return Pair(importedEntriesCount, invalidEntriesCount)
    }

    // Database insertion operation
    private val insertStatement = database.compileStatement(DatabaseContract.INSERT_QUERY)
    private fun insertPostalCode(postalCode: Long, extension: Long, locality: String) =
        insertStatement.run {
            clearBindings()
            bindLong(1, postalCode)
            bindLong(2, extension)
            bindString(3, locality)
            execute()
            clearBindings()
        }

    companion object {
        const val TAG = "CsvImportTask"
        const val TRANSACTION_SIZE = 1000L
    }
}