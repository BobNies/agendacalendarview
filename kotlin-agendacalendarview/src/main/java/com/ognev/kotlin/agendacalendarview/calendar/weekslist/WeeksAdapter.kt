package com.ognev.kotlin.agendacalendarview.calendar.weekslist

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ognev.kotlin.agendacalendarview.CalendarManager
import com.ognev.kotlin.agendacalendarview.R
import com.ognev.kotlin.agendacalendarview.models.IWeekItem
import com.ognev.kotlin.agendacalendarview.utils.BusProvider
import com.ognev.kotlin.agendacalendarview.utils.DateHelper
import com.ognev.kotlin.agendacalendarview.utils.Events
import java.text.SimpleDateFormat
import java.util.*

class WeeksAdapter(private val mContext: Context,
                   private val mToday: Calendar,
                   val monthColor: Int,
                   val selectedDayTextColor: Int,
                   val currentDayTextColor: Int,
                   val pastDayTextColor: Int,
                   val circleColor: Drawable?,
                   val cellPastBackgroundColor: Int,
                   val cellNowadaysDayColor: Int
) : RecyclerView.Adapter<WeeksAdapter.WeekViewHolder>() {
    override fun getItemCount(): Int {
        return weeksList.size
    }

    private val weeksList: List<IWeekItem>
    var isDragging: Boolean = false
        set(dragging) {
            if (dragging != this.isDragging) {
                field = dragging
                notifyItemRangeChanged(0, weeksList.size)
            }
        }
    var isAlphaSet: Boolean = false

    init {
        weeksList = CalendarManager.instance!!.weeks
    }


    fun updateWeeksItems() {
        notifyDataSetChanged()
    }


    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_week, parent, false)
        return WeekViewHolder(view)
    }

    override
    fun onBindViewHolder(weekViewHolder: WeekViewHolder, position: Int) {
        val weekItem = weeksList[position]
        weekViewHolder.bindWeek(weekItem, mToday)
    }


    inner class WeekViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * List of layout containers for each day
         */
        private var mCells: List<LinearLayout>? = null
        private val mTxtMonth: TextView = itemView.findViewById(R.id.month_label) as TextView
        private val mMonthBackground: FrameLayout = itemView.findViewById(R.id.month_background) as FrameLayout

        init {
            val daysContainer = itemView.findViewById(R.id.week_days_container) as LinearLayout
            setUpChildren(daysContainer)
        }

        fun bindWeek(weekItem: IWeekItem, today: Calendar) {
            setUpMonthOverlay()

            val dayItems = weekItem.dayItems

            for (c in dayItems.indices) {
                val dayItem = dayItems[c]
                val cellItem = mCells!![c]
                val txtDay = cellItem.findViewById(R.id.view_day_day_label) as TextView
                val txtMonth = cellItem.findViewById(R.id.view_day_month_label) as TextView
                val circleView = cellItem.findViewById<View>(R.id.view_day_circle_selected)
                val point = cellItem.findViewById<View>(R.id.point)
                cellItem.setOnClickListener { BusProvider.instance.send(Events.DayClickedEvent(dayItem)) }

                circleView.background = circleColor
                txtMonth.visibility = View.GONE
                txtDay.setTextColor(pastDayTextColor)
                txtMonth.setTextColor(monthColor)
                circleView.visibility = View.GONE

                //        point.setVisibility(dayItem.hasEvents() ? View.VISIBLE : View.GONE);

                txtDay.setTypeface(null, Typeface.NORMAL)
                txtMonth.setTypeface(null, Typeface.NORMAL)

                // Display the day
                txtDay.text = dayItem.value.toString()

                // Highlight first day of the month
                if (dayItem.isFirstDayOfMonth && !dayItem.isSelected) {
                    txtMonth.visibility = View.VISIBLE
                    txtMonth.text = dayItem.month
                    txtDay.setTypeface(null, Typeface.BOLD)
                    txtMonth.setTypeface(null, Typeface.BOLD)
                }

                // Check if this day is in the past
                if (today.time.after(dayItem.date) && !DateHelper.sameDate(today, dayItem.date)) {
                    txtDay.setTextColor(pastDayTextColor)
                    txtMonth.setTextColor(monthColor)
                    cellItem.setBackgroundColor(cellPastBackgroundColor)
                } else {
                    cellItem.setBackgroundColor(cellNowadaysDayColor)
                }

                // Highlight the cell if this day is today
                if (dayItem.isToday && !dayItem.isSelected) {
                    txtDay.setTextColor(currentDayTextColor)
                }

                // Show a circle if the day is selected
                if (dayItem.isSelected) {
                    txtDay.setTextColor(selectedDayTextColor)
                    circleView.visibility = View.VISIBLE
                    //          GradientDrawable drawable = (GradientDrawable) circleView.getBackground();
                    //          drawable.setStroke((int) (1 * Resources.getSystem().getDisplayMetrics().density), mDayTextColor);
                    //          point.setVisibility(View.GONE);
                }

                point.visibility = if (dayItem.hasEvents()) View.VISIBLE else View.INVISIBLE

                // Check if the month label has to be displayed
                if (dayItem.value == 15) {
                    mTxtMonth.visibility = View.VISIBLE
                    val monthDateFormat = SimpleDateFormat(mContext.getString(R.string.month_half_name_format), CalendarManager.instance!!.locale)
                    var month = monthDateFormat.format(weekItem.date).toUpperCase(Locale.getDefault())
                    if (today.get(Calendar.YEAR) != weekItem.year) {
                        month += String.format(" %d", weekItem.year)
                    }
                    mTxtMonth.text = month
                }
            }
        }

        private fun setUpChildren(daysContainer: LinearLayout) {
            mCells = ArrayList()
            for (i in 0 until daysContainer.childCount) {
                (mCells as ArrayList<LinearLayout>).add(daysContainer.getChildAt(i) as LinearLayout)
            }
        }

        private fun setUpMonthOverlay() {
            mTxtMonth.visibility = View.GONE

            if (isDragging) {
                val animatorSetFadeIn = AnimatorSet()
                animatorSetFadeIn.duration = FADE_DURATION
                val animatorTxtAlphaIn = ObjectAnimator.ofFloat(mTxtMonth, "alpha", mTxtMonth.alpha, 1f)

                animatorSetFadeIn.playTogether(
                        animatorTxtAlphaIn
                )
                animatorSetFadeIn.addListener(object : Animator.AnimatorListener {
                    override
                    fun onAnimationStart(animation: Animator) {

                    }

                    override
                    fun onAnimationEnd(animation: Animator) {
                        isAlphaSet = true
                    }

                    override
                    fun onAnimationCancel(animation: Animator) {

                    }

                    override
                    fun onAnimationRepeat(animation: Animator) {

                    }
                })
                animatorSetFadeIn.start()
            } else {
                val animatorSetFadeOut = AnimatorSet()
                animatorSetFadeOut.duration = FADE_DURATION
                val animatorTxtAlphaOut = ObjectAnimator.ofFloat(mTxtMonth, "alpha", mTxtMonth.alpha, 0f)
                animatorSetFadeOut.playTogether(
                        animatorTxtAlphaOut
                )
                animatorSetFadeOut.addListener(object : Animator.AnimatorListener {
                    override
                    fun onAnimationStart(animation: Animator) {

                    }

                    override
                    fun onAnimationEnd(animation: Animator) {
                        isAlphaSet = false
                    }

                    override
                    fun onAnimationCancel(animation: Animator) {

                    }

                    override
                    fun onAnimationRepeat(animation: Animator) {

                    }
                })
                animatorSetFadeOut.start()
            }

            if (isAlphaSet) {
                //mMonthBackground.setAlpha(1f);
                mTxtMonth.alpha = 1f
            } else {
                //mMonthBackground.setAlpha(0f);
                mTxtMonth.alpha = 0f
            }
        }
    }

    companion object {

        const val FADE_DURATION: Long = 250
    }

    // endregion
}
