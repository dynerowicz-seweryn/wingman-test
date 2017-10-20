package me.dynerowicz.wtest.tasks

interface DownloadProgressListener {
    fun onDownloadProgressUpdate(new: Int)
    fun onDownloadComplete(success: Boolean)
}