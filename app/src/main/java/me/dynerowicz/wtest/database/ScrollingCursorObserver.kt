package me.dynerowicz.wtest.database

interface ScrollingCursorObserver {
    fun onMoreResultsAvailable(count: Int)
    fun onEndOfResults()
}