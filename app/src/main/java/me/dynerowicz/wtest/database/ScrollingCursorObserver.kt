package me.dynerowicz.wtest.database

import android.util.Log

interface ScrollingCursorObserver {
    fun onMoreResultsAvailable(positionStart: Int, rowCount: Int) {
        Log.v(TAG, "onMoreResultsAvailable : $positionStart, $rowCount")
    }

    fun onEndOfResults() {
        Log.v(TAG, "onEndOfResults")
    }

    companion object {
        const val TAG = "ScrollingCursorObserver"
    }
}