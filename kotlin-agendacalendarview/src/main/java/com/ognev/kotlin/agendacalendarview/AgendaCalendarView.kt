package com.ognev.kotlin.agendacalendarview

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ognev.kotlin.agendacalendarview.agenda.AgendaAdapter
import com.ognev.kotlin.agendacalendarview.agenda.AgendaView
import com.ognev.kotlin.agendacalendarview.calendar.CalendarView
import com.ognev.kotlin.agendacalendarview.models.CalendarEvent
import com.ognev.kotlin.agendacalendarview.models.IDayItem
import com.ognev.kotlin.agendacalendarview.models.IWeekItem
import com.ognev.kotlin.agendacalendarview.render.DefaultEventAdapter
import com.ognev.kotlin.agendacalendarview.render.EventAdapter
import com.ognev.kotlin.agendacalendarview.utils.BusProvider
import com.ognev.kotlin.agendacalendarview.utils.Events
import se.emilsjolander.stickylistheaders.StickyListHeadersListView

/**
 * View holding the agenda and calendar view together.
 */
class AgendaCalendarView : FrameLayout, StickyListHeadersListView.OnStickyHeaderChangedListener {

    private var mCalendarView: CalendarView? = null
    lateinit var agendaView: AgendaView
        private set

    private var mCalendarHeaderColor: Int = 0
    private var monthCalendarColor: Int = 0
    private var selectedDayTextColor: Int = 0
    private var mCalendarPastDayTextColor: Int = 0
    private var circleBackgroundColor: Drawable? = null
    private var cellPastBackgroundColor: Int = 0
    private var cellNowadaysDayColor: Int = 0
    private var mCalendarCurrentDayColor: Int = 0
    private var mFabColor: Int = 0
    private var calendarController: CalendarController? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.AgendaCalendarView, 0, 0)
        mCalendarHeaderColor = a.getColor(
            R.styleable.AgendaCalendarView_calendarHeaderColor,
            ContextCompat.getColor(context, R.color.theme_primary)
        )
        monthCalendarColor = a.getColor(
            R.styleable.AgendaCalendarView_calendarMonthTextColor,
            ContextCompat.getColor(context, R.color.theme_text_icons)
        )
        selectedDayTextColor = a.getColor(
            R.styleable.AgendaCalendarView_calendarSelectedDayTextColor,
            ContextCompat.getColor(context, R.color.theme_text_icons)
        )
        mCalendarCurrentDayColor = a.getColor(
            R.styleable.AgendaCalendarView_calendarCurrentDayTextColor,
            ContextCompat.getColor(context, R.color.calendar_text_current_day)
        )
        mCalendarPastDayTextColor = a.getColor(
            R.styleable.AgendaCalendarView_calendarPastDayTextColor,
            ContextCompat.getColor(context, R.color.theme_light_primary)
        )
        circleBackgroundColor = a.getDrawable(R.styleable.AgendaCalendarView_circleBackgroundColor)
        cellNowadaysDayColor = a.getColor(
            R.styleable.AgendaCalendarView_cellNowadaysDayColor,
            ContextCompat.getColor(context, R.color.white)
        )
        cellPastBackgroundColor = a.getColor(
            R.styleable.AgendaCalendarView_cellPastBackgroundColor,
            ContextCompat.getColor(context, R.color.calendar_past_days_bg)
        )
        mFabColor = a.getColor(
            R.styleable.AgendaCalendarView_fabColor,
            ContextCompat.getColor(context, R.color.theme_accent)
        )
        a.recycle()

        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_agendacalendar, this, true)

        alpha = 0f
    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        mCalendarView = findViewById(R.id.calendar_view)
        agendaView = findViewById(R.id.agenda_view)
        mCalendarView?.findViewById<LinearLayout>(R.id.cal_day_names)
            ?.setBackgroundColor(mCalendarHeaderColor)

        BusProvider.instance.toObservable()
            .subscribe { event ->
                if (event is Events.DayClickedEvent) {
                    calendarController?.onDaySelected((event).day)
                } else if (event is Events.EventsFetched) {
                    val alphaAnimation =
                        ObjectAnimator.ofFloat(this, "alpha", alpha, 1f).setDuration(500)
                    alphaAnimation.addListener(object : Animator.AnimatorListener {
                        override
                        fun onAnimationStart(animation: Animator) {

                        }

                        override
                        fun onAnimationEnd(animation: Animator) {

                        }

                        override
                        fun onAnimationCancel(animation: Animator) {

                        }

                        override
                        fun onAnimationRepeat(animation: Animator) {

                        }
                    })
                    alphaAnimation.start()
                }
            }
    }


    override
    fun onStickyHeaderChanged(
        l: StickyListHeadersListView,
        header: View, itemPosition: Int, headerId: Long
    ) {
        if (CalendarManager.instance?.events?.size ?: 0 > 0) {
            CalendarManager.instance?.events?.get(itemPosition)?.let { event ->
                mCalendarView?.scrollToDate(event)
                calendarController?.onScrollToDate(event.instanceDay)
            }
        }
    }


    fun setCallbacks(calendarController: CalendarController) {
        this.calendarController = calendarController
    }

    fun init(
        lWeeks: MutableList<IWeekItem>,
        lDays: MutableList<IDayItem>,
        lEvents: MutableList<CalendarEvent>,
        sampleAgendaAdapter: DefaultEventAdapter
    ) {

        CalendarManager.getInstance(context).loadCal(lWeeks, lDays, lEvents)

        // Feed our views with weeks MutableList and events
        mCalendarView?.init(
            CalendarManager.getInstance(context), monthCalendarColor,
            selectedDayTextColor, mCalendarCurrentDayColor, mCalendarPastDayTextColor,
            circleBackgroundColor, cellPastBackgroundColor, cellNowadaysDayColor
        )

        // Load agenda events and scroll to current day
        val agendaAdapter = AgendaAdapter()
        agendaView.agendaListView.adapter = agendaAdapter
        agendaView.agendaListView.setOnStickyHeaderChangedListener(this)

        // notify that actually everything is loaded
        BusProvider.instance.send(Events.EventsFetched())
        //    Log.d(LOG_TAG, "CalendarEventTask finished");

        // add default event renderer
        addEventRenderer(sampleAgendaAdapter)
    }

    @Suppress("UNCHECKED_CAST")
    private fun addEventRenderer(@NonNull eventAdapter: EventAdapter<*>) {
        val agendaAdapter = agendaView.agendaListView.adapter as AgendaAdapter
        agendaAdapter.addEventRenderer(eventAdapter as EventAdapter<CalendarEvent>)
    }

    fun showProgress() {
        (findViewById<SwipeRefreshLayout>(R.id.refresh_layout)).isRefreshing = true
    }

    fun hideProgress() {
        (findViewById<SwipeRefreshLayout>(R.id.refresh_layout)).isRefreshing = false
    }

    fun isCalendarLoading(): Boolean {
        return (findViewById<SwipeRefreshLayout>(R.id.refresh_layout)).isRefreshing
    }

}
