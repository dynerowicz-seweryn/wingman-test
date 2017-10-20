package me.dynerowicz.wtest.database

import me.dynerowicz.wtest.BuildConfig
import me.dynerowicz.wtest.tasks.FileDownloaderTask
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class FileDownloaderTaskTest {
    lateinit var downloader : FileDownloaderTask

    @Before
    fun setup() {
        ShadowLog.stream = System.out
        downloader = FileDownloaderTask(RuntimeEnvironment.application)
    }

    @After
    fun teardown() {

    }

    @Test
    fun testHeaderDownload() {
        downloader.execute()
    }
}