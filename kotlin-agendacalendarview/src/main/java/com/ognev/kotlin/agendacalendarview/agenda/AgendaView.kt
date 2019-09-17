package com.ognev.kotlin.agendacalendarview.agenda

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ognev.kotlin.agendacalendarview.CalendarManager
import com.ognev.kotlin.agendacalendarview.R
import com.ognev.kotlin.agendacalendarview.utils.BusProvider
import com.ognev.kotlin.agendacalendarview.utils.Events

class AgendaView : FrameLayout {

    lateinit var agendaListView: AgendaListView
        private set

    private var mShadowView: View? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_agenda, this, true)

        (findViewById<SwipeRefreshLayout>(R.id.refresh_layout)).isEnabled = false
    }


    override fun onFinishInflate() {
        super.onFinishInflate()

        agendaListView = findViewById(R.id.agenda_listview)
        mShadowView = findViewById(R.id.view_shadow)

        BusProvider.instance.toObservable()
            .subscribe { event ->
                when (event) {
                    is Events.DayClickedEvent -> agendaListView.scrollToCurrentDate(event.calendar)
                    is Events.CalendarScrolledEvent -> {
                        val offset = (3 * resources.getDimension(R.dimen.day_cell_height))
                        translateList(offset.toInt())
                    }
                    is Events.EventsFetched -> {
                        (agendaListView.adapter as AgendaAdapter).updateEvents()

                        viewTreeObserver.addOnGlobalLayoutListener(
                            object : ViewTreeObserver.OnGlobalLayoutListener {
                                override
                                fun onGlobalLayout() {
                                    if (width != 0 && height != 0) {
                                        // display only two visible rows on the calendar view
                                        val layoutParams = layoutParams as MarginLayoutParams
                                        val height = height
                                        val margin =
                                            (context.resources.getDimension(R.dimen.calendar_header_height) + 2 * context.resources.getDimension(
                                                R.dimen.day_cell_height
                                            ))
                                        layoutParams.height = height
                                        layoutParams.setMargins(0, margin.toInt(), 0, 0)
                                        setLayoutParams(layoutParams)
                                        //todo
                                        CalendarManager.instance?.let { cm ->
                                            if (cm.events.isNotEmpty()) {
                                                agendaListView.scrollToCurrentDate(cm.today)
                                            }
                                        }

                                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                                    }
                                }
                            }
                        )
                    }
                    is Events.ForecastFetched -> (agendaListView.adapter as AgendaAdapter).updateEvents()
                }
            }
    }

    override
    fun dispatchTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN ->
                // if the user touches the listView, we put it back to the top
                translateList(0)
            else -> {
            }
        }

        return super.dispatchTouchEvent(event)
    }

    fun translateList(targetY: Int) {
        if (targetY != translationY.toInt()) {
            val mover = ObjectAnimator.ofFloat(this, "translationY", targetY.toFloat())
            mover.duration = 150
            mover.addListener(object : Animator.AnimatorListener {
                override
                fun onAnimationStart(animation: Animator) {
                    mShadowView?.visibility = GONE
                }

                override
                fun onAnimationEnd(animation: Animator) {
                    if (targetY == 0) {
                        BusProvider.instance.send(Events.AgendaListViewTouchedEvent())
                    }
                    mShadowView?.visibility = VISIBLE
                }

                override
                fun onAnimationCancel(animation: Animator) {

                }

                override
                fun onAnimationRepeat(animation: Animator) {

                }
            })
            mover.start()
        }
    }

}
