package com.ognev.kotlin.agendacalendarview.models

import java.util.Date

interface IDayItem {

    var date: Date

    var value: Int

    var isToday: Boolean

    var isSelected: Boolean

    var isFirstDayOfMonth: Boolean

    var month: String

    var dayOfWeek: Int

    override fun toString(): String

    fun setHasEvents(hasEvents: Boolean)

    fun hasEvents(): Boolean
}
