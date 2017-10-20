package me.dynerowicz.wtest.tasks

interface CsvImportListener {
    fun onImportProgressUpdate(new: Int)
    fun onImportComplete(result: Pair<Long, Long>)
}