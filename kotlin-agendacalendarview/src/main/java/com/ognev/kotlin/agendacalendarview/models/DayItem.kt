package com.ognev.kotlin.agendacalendarview.models

import com.ognev.kotlin.agendacalendarview.CalendarManager
import com.ognev.kotlin.agendacalendarview.utils.DateHelper
import java.util.*

/**
 * Day model class.
 */
class DayItem(

    override var date: Date,
    override var value: Int,
    override var isToday: Boolean,
    override var month: String
) : IDayItem {

    override var dayOfWeek: Int = 0
    override var isFirstDayOfMonth: Boolean = false
    override var isSelected: Boolean = false
    private var hasEvents: Boolean = false

    override fun setHasEvents(hasEvents: Boolean) {
        this.hasEvents = hasEvents
    }

    override fun hasEvents(): Boolean {
        return hasEvents
    }

    // endregion

    override
    fun toString(): String {
        return "DayItem{"
            .plus("Date='")
            .plus(date.toString())
            .plus(", value=")
            .plus(value)
            .plus('}')
    }

    companion object {
        fun buildDayItemFromCal(calendar: Calendar): DayItem {
            val date = calendar.time
            val isToday = DateHelper.sameDate(calendar, CalendarManager.instance!!.today)
            val value = calendar.get(Calendar.DAY_OF_MONTH)
            val dayItem = DayItem(
                date,
                value,
                isToday,
                CalendarManager.instance!!.monthHalfNameFormat!!.format(date)
            )
            if (value == 1) {
                dayItem.isFirstDayOfMonth = true
            }
            dayItem.isToday = isToday
            return dayItem
        }
    }
}
