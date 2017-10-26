package me.dynerowicz.wtest.database

interface ScrollingCursorObserver {
    fun onMoreResultsAvailable(positionStart: Int, rowCount: Int)
    fun onEndOfResults()
}