package me.dynerowicz.wtest.database

import android.database.sqlite.SQLiteDatabase
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
class CsvImporterTask20EntriesTest {

    private lateinit var databaseHelper: DatabaseHelper
    lateinit var database: SQLiteDatabase

    @Before
    fun setUp() {
        ShadowLog.stream = System.out

        databaseHelper = DatabaseHelper(RuntimeEnvironment.application)
        database = databaseHelper.writableDatabase

        val csvFile = File("app/src/test/resources/codigos_postais-20.csv")
        val asyncTask = CsvImporterTask(database, csvFile, null)
        asyncTask.execute()
        ShadowApplication.runBackgroundTasks()

        val (importedEntries, invalidEntries) = asyncTask.get()

        Assert.assertTrue(importedEntries == 20L)
        Assert.assertTrue(invalidEntries == 0L)
    }

    @After
    fun tearDown() {
        database.close()
        databaseHelper.close()
    }

    @Test
    fun databaseHas20Entries() {
        val query = "SELECT * FROM ${DatabaseContract.TABLE_NAME}"
        val cursor = database.rawQuery(query, null)
        Assert.assertTrue(cursor.count == 20)
        cursor.close()
    }

    @Test
    fun databaseContains3750043() {
        val query = "SELECT * FROM ${DatabaseContract.TABLE_NAME} WHERE ${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}=3750043"
        val cursor = database.rawQuery(query, null)
        Assert.assertTrue(cursor.count == 1)
        cursor.close()
    }

    @Test
    fun databaseContains3750043ForAlmasDaAreosa() {
        val query = "SELECT ${DatabaseContract.COLUMN_LOCALITY} FROM ${DatabaseContract.TABLE_NAME} WHERE ${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}=3750043"
        val cursor = database.rawQuery(query, null)
        cursor.moveToFirst()
        Assert.assertTrue(cursor.getString(0) == "Almas da Areosa")
        cursor.close()
    }

    @Test
    fun databaseDoesNotContain3750902() {
        val query = "SELECT ${DatabaseContract.COLUMN_LOCALITY} FROM ${DatabaseContract.TABLE_NAME} WHERE ${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}=3750902"
        val cursor = database.rawQuery(query, null)
        Assert.assertTrue(cursor.count == 0)
        cursor.close()
    }

    @Test
    fun databaseDoesNotContain3750016ForLandiosa() {
        val query = "SELECT ${DatabaseContract.COLUMN_LOCALITY} FROM ${DatabaseContract.TABLE_NAME} WHERE ${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}=3750016"
        val cursor = database.rawQuery(query, null)
        cursor.moveToFirst()
        Assert.assertTrue(cursor.getString(0) != "Landiosa")
        cursor.close()
    }
}