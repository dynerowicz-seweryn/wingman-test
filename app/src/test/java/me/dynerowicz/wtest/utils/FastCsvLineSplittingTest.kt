package me.dynerowicz.wtest.database

import me.dynerowicz.wtest.utils.parseCsvLine
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.io.BufferedReader
import java.io.File

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class FastCsvLineSplittingTest {
    private lateinit var csvFileReader: BufferedReader

    @Before
    fun setup() {
        ShadowLog.stream = System.out
        csvFileReader = File("app/src/test/resources/codigos_postais-1000.csv").bufferedReader()
    }

    @After
    fun teardown() {
        csvFileReader.close()
    }

    @Test
    fun fileContainsValidEntries() {
        val fields = Array(3) { StringBuilder() }

        while(csvFileReader.ready()) {
            fields.forEach { it.setLength(0) }

            val fieldCount = fields.parseCsvLine(csvFileReader.readLine(), 0, 11, 12)
            println("Extracted fields = ${fields[1]}-${fields[2]} : ${fields[0]}")
            Assert.assertTrue(fieldCount == 14)
        }
    }
}
