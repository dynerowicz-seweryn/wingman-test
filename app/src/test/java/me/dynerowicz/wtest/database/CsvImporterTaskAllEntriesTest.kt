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
class CsvImporterTaskAllEntriesTest : CsvImportListener {

    private lateinit var databaseHelper: DatabaseHelper
    lateinit var database: SQLiteDatabase

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        databaseHelper = DatabaseHelper(RuntimeEnvironment.application)
        database = databaseHelper.writableDatabase

        val csvFile = File("app/src/test/resources/codigos_postais-full.csv")
        val asyncTask = CsvImporterTask(database, csvFile, this)
        asyncTask.execute()
        ShadowApplication.runBackgroundTasks()

        val (importedEntries, invalidEntries) = asyncTask.get()

        Assert.assertTrue(importedEntries == 314772L)
        Assert.assertTrue(invalidEntries == 0L)
    }

    @After
    fun tearDown() {
        database.close()
        databaseHelper.close()
    }

    @Test
    fun testHas314772EntriesInDatabase() {
        val query = "SELECT * FROM ${DatabaseContract.TABLE_NAME}"
        val cursor = database.rawQuery(query, null)
        Assert.assertTrue(cursor.count == 314772)
        cursor.close()
    }
}