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
        val query = "SELECT * FROM ${DatabaseContract.POSTAL_CODES_TABLE}"
        val cursor = database.rawQuery(query, null)
        Assert.assertTrue(cursor.count == 20)
        cursor.close()
    }

    @Test
    fun databaseContainsStuff() {
        val query = " SELECT PC.${DatabaseContract.COLUMN_POSTAL_CODE_WITH_EXTENSION}, LN.${DatabaseContract.COLUMN_NAME} " +
                    " FROM ${DatabaseContract.POSTAL_CODES_TABLE} PC INNER JOIN ${DatabaseContract.LOCALITIES_TABLE} LN " +
                    " WHERE PC.${DatabaseContract.COLUMN_LOCALITY_IDENTIFIER} == LN.${DatabaseContract.COLUMN_ID}"
        val cursor = database.rawQuery(query, null)

        while(cursor.moveToNext())
            println("Row: ${cursor.getLong(0)}; ${cursor.getString(1)}")

        cursor.close()
    }

    @Test
    fun databaseContains3750043() {
        val query = QueryBuilder(inputs = "3750043").toString()
        val cursor = database.rawQuery(query, null)

        while (cursor.moveToNext()) {
            println("Postal code row : ${cursor.getLong(0)}, ${cursor.getString(1)}")
        }

        Assert.assertTrue(cursor.count == 1)
        cursor.close()
    }

    @Test
    fun databaseContains3750043ForAlmasDaAreosa() {
        val query = QueryBuilder(inputs = "3750043").toString()
        val cursor = database.rawQuery(query, null)
        cursor.moveToFirst()
        Assert.assertTrue(cursor.getString(1) == "Almas da Areosa")
        cursor.close()
    }

    @Test
    fun databaseDoesNotContain3750902() {
        val query = QueryBuilder(inputs = "3750902").toString()
        val cursor = database.rawQuery(query, null)
        Assert.assertTrue(cursor.count == 0)
        cursor.close()
    }

    @Test
    fun databaseDoesNotContain3750016ForLandiosa() {
        val query = QueryBuilder(inputs = "3750016").toString()
        val cursor = database.rawQuery(query, null)
        cursor.moveToFirst()
        Assert.assertTrue(cursor.getString(1) != "Landiosa")
        cursor.close()
    }
}