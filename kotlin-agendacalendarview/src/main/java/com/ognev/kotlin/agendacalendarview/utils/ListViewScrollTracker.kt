package com.ognev.kotlin.agendacalendarview.utils


import android.util.SparseArray
import android.util.SparseIntArray
import com.ognev.kotlin.agendacalendarview.agenda.AgendaListView

/**
 * Helper class calculating the scrolling distance in the AgendaListView.
 */
class ListViewScrollTracker
// region Constructor and Accessor(s)

    (private val mListView: AgendaListView) {
    private var mPositions: SparseArray<Int>? = null
    private val mListViewItemHeights = SparseIntArray()
    private var mFirstVisiblePosition: Int = 0
    private var referencePosition = -1
        private set // Position of the current date in the Agenda listView

    // endregion

    // region Public methods

    /**
     * Call from an AbsListView.OnScrollListener to calculate the incremental offset (change in
     * scroll offset
     * since the last calculation).

     * @param firstVisiblePosition First visible item position in the list.
     * *
     * @param visibleItemCount     Number of visible items in the list.
     * *
     * @return The incremental offset, or 0 if it wasn't possible to calculate the offset.
     */
    fun calculateIncrementalOffset(firstVisiblePosition: Int, visibleItemCount: Int): Int {
        // Remember previous positions, if any
        val previousPositions = mPositions

        // Store new positions
        mPositions = SparseArray()
        for (i in 0 until visibleItemCount) {
            mPositions?.put(firstVisiblePosition + i, mListView.getListChildAt(i).top)
        }

        if (previousPositions != null) {
            // Find position which exists in both mPositions and previousPositions, then return the difference
            // of the new and old Y values.
            for (i in 0 until previousPositions.size()) {
                val previousPosition = previousPositions.keyAt(i)
                val previousTop = previousPositions.get(previousPosition)
                val newTop = mPositions?.get(previousPosition)
                if (newTop != null) {
                    return newTop.toInt() - previousTop.toInt()
                }
            }
        }

        return 0 // No view's position was in both previousPositions and mPositions
    }

    /**
     * Call from an AbsListView.OnScrollListener to calculate the scrollY (Here
     * we definite as the distance in pixels compared to the position representing the current
     * date).

     * @param firstVisiblePosition First visible item position in the list.
     * *
     * @param visibleItemCount     Number of visible items in the list.
     * *
     * @return Distance in pixels compared to current day position (negative if firstVisiblePosition less than mReferencePosition)
     */
    fun calculateScrollY(firstVisiblePosition: Int, visibleItemCount: Int): Int {
        mFirstVisiblePosition = firstVisiblePosition
        if (referencePosition < 0) {
            referencePosition = mFirstVisiblePosition
        }

        if (visibleItemCount > 0) {
            val c = mListView.getListChildAt(0) // this is the first visible row
            var scrollY = -c.top
            mListViewItemHeights.put(firstVisiblePosition, c.measuredHeight)

            if (mFirstVisiblePosition >= referencePosition) {
                for (i in referencePosition until firstVisiblePosition) {
                    scrollY += mListViewItemHeights.get(i) // add all heights of the views that are gone
                }
                return scrollY
            } else {
                for (i in referencePosition - 1 downTo firstVisiblePosition) {
                    scrollY -= mListViewItemHeights.get(i)
                }
                return scrollY
            }

        }
        return 0
    }

    fun clear() {
        mPositions = null
    }

    // endregion
}
