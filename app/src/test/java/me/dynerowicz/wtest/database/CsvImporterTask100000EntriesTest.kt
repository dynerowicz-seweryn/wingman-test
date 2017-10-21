package me.dynerowicz.wtest.database

import android.database.sqlite.SQLiteDatabase
import me.dynerowicz.wtest.tasks.CsvImportListener
import me.dynerowicz.wtest.tasks.CsvImporterTask
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLog
import java.io.File

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class CsvImporterTask100000EntriesTest : CsvImportListener {

    private lateinit var databaseHelper: DatabaseHelper
    lateinit var database: SQLiteDatabase

    @Before
    fun setUp() {
        ShadowLog.stream = System.out

        databaseHelper = DatabaseHelper(RuntimeEnvironment.application)
        database = databaseHelper.writableDatabase

        val csvFile = File("app/src/test/resources/codigos_postais-100000.csv")
        val asyncTask = CsvImporterTask(database, csvFile, this)
        asyncTask.execute()
        ShadowApplication.runBackgroundTasks()

        val (importedEntries, invalidEntries) = asyncTask.get()

        Assert.assertTrue(importedEntries == 100000L)
        Assert.assertTrue(invalidEntries == 0L)
    }

    @After
    fun tearDown() {
        database.close()
        databaseHelper.close()
    }

    @Test
    fun testHas100000EntriesInDatabase() {
        val query = "SELECT * FROM ${DatabaseContract.TABLE_NAME}"
        val cursor = database.rawQuery(query, null)
        Assert.assertTrue(cursor.count == 100000)
        cursor.close()
    }

    @Test
    fun testPerformSelect3750() {
        val query = "SELECT * FROM ${DatabaseContract.TABLE_NAME} WHERE ${DatabaseContract.COLUMN_POSTAL_CODE} = 3750"
        val cursor = database.rawQuery(query, null)
        Assert.assertTrue(cursor.count == 1184)
        cursor.close()
    }

    @Test
    fun testPerformSelect3700to3800() {
        val query = "SELECT * FROM ${DatabaseContract.TABLE_NAME} WHERE ${DatabaseContract.COLUMN_POSTAL_CODE} BETWEEN 3700 AND 3800"
        val cursor = database.rawQuery(query, null)
        Assert.assertTrue(cursor.count == 7375)
        cursor.close()
    }

    override fun onImportProgressUpdate(new: Int) = println("Import progress: $new %")
    override fun onImportComplete(result: Pair<Long, Long>) = println("Import completed")
    override fun onImportCancelled() = println("Import cancelled")
}