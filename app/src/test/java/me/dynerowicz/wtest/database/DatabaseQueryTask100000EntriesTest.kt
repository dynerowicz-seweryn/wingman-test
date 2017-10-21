package me.dynerowicz.wtest.database

import android.database.sqlite.SQLiteDatabase
import me.dynerowicz.wtest.tasks.CsvImportListener
import me.dynerowicz.wtest.tasks.CsvImporterTask
import me.dynerowicz.wtest.tasks.DatabaseQueryListener
import me.dynerowicz.wtest.tasks.DatabaseQueryTask
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
class DatabaseQueryTask100000EntriesTest : CsvImportListener, DatabaseQueryListener {
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
    fun databaseContains100000Rows() {
        val query = "SELECT * FROM ${DatabaseContract.TABLE_NAME}"
        val cursor = database.rawQuery(query, null)
        Assert.assertTrue(cursor.count == 100000)
        cursor.close()
    }

    @Test
    fun databaseContains1184RowsWithPostalCode3750() {
        val query = "SELECT * FROM ${DatabaseContract.TABLE_NAME} WHERE ${DatabaseContract.COLUMN_POSTAL_CODE} = 3750"
        val task = DatabaseQueryTask(database, this)
        task.execute(query)
        ShadowApplication.runBackgroundTasks()
        val result = task.get()
        Assert.assertTrue(result.size == 1184)
    }
}