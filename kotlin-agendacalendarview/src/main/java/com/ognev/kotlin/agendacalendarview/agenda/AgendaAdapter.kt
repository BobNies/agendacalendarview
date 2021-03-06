package com.ognev.kotlin.agendacalendarview.agenda

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.NonNull
import com.ognev.kotlin.agendacalendarview.CalendarManager
import com.ognev.kotlin.agendacalendarview.models.CalendarEvent
import com.ognev.kotlin.agendacalendarview.render.EventAdapter
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter
import java.util.*

/**
 * Adapter for the agenda, implements StickyListHeadersAdapter.
 * Days as sections and CalendarEvents as list items.
 */
class AgendaAdapter : BaseAdapter(), StickyListHeadersAdapter {

    override fun getCount(): Int {
        return CalendarManager.instance?.events?.size ?: 0
    }

    private val mRenderers = ArrayList<EventAdapter<CalendarEvent>>()

    fun updateEvents() {
        notifyDataSetChanged()
    }

    override
    fun getHeaderView(position: Int, convertView: View?, parent: ViewGroup): View {
        var agendaHeaderView = convertView
        val eventAdapter: EventAdapter<CalendarEvent>? = mRenderers[0]

        if (agendaHeaderView == null) {
            agendaHeaderView = LayoutInflater.from(parent.context)
                .inflate(eventAdapter!!.getHeaderLayout(), parent, false)
        }

        if (CalendarManager.instance?.events?.isNotEmpty() == true) {
            agendaHeaderView?.let {
                eventAdapter?.getHeaderItemView(
                    it,
                    getItem(position).instanceDay
                )
            }
        }

        return agendaHeaderView!!
    }


    override
    fun getHeaderId(position: Int): Long {
        return (if (CalendarManager.instance!!.events.isEmpty()) 0
        else CalendarManager.instance!!.events[position].instanceDay.timeInMillis).toLong()
    }

    override
    fun getItem(position: Int): CalendarEvent {
        return CalendarManager.instance!!.events[position]
    }

    override
    fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override
    fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        var eventAdapter: EventAdapter<CalendarEvent> = mRenderers[0]
        val event = getItem(position)

        // Search for the correct event renderer
        for (renderer in mRenderers) {
            if (event.javaClass.isAssignableFrom(renderer.renderType)) {
                eventAdapter = renderer
                break
            }
        }

        view = LayoutInflater.from(parent.context)
            .inflate(
                eventAdapter.getEventLayout(CalendarManager.instance!!.events[position].hasEvent()),
                parent,
                false
            )

        eventAdapter.getEventItemView(view!!, event, position)

        return view
    }

    fun addEventRenderer(@NonNull adapter: EventAdapter<CalendarEvent>) {
        mRenderers.add(adapter)
    }

}
