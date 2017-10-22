package me.dynerowicz.wtest.download

import me.dynerowicz.wtest.tasks.CsvDownloadListener
import me.dynerowicz.wtest.tasks.CsvDownloadTask
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLog
import java.net.URL

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class CsvDownloadTaskTest : CsvDownloadListener {

    private lateinit var csvDownloadTask: CsvDownloadTask

    @Before
    fun setUp() {
        ShadowLog.stream = System.out

        csvDownloadTask = CsvDownloadTask(
            RuntimeEnvironment.application,
            URL("https://raw.githubusercontent.com/centraldedados/codigos_postais/master/data/codigos_postais.csv"),
            numberOfCsvEntriesFiles = 2,
            downloadListener = this
        )

    }

    @After fun tearDown() {}

    @Test
    fun testHas314772EntriesInDatabase() {
        csvDownloadTask.execute()
        ShadowApplication.runBackgroundTasks()
    }
}