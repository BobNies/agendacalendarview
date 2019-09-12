package com.ognev.kotlin.agendacalendarview.render

import android.view.View
import androidx.annotation.LayoutRes
import java.util.*

/**
 * Base class for helping layout rendering
 */
abstract class EventAdapter<T> {

    abstract fun getEventItemView(view: View, event: T, position: Int)

    abstract fun getHeaderItemView(view: View, day: Calendar)

    @LayoutRes
    abstract fun getEventLayout(isEmptyEvent: Boolean): Int

    @LayoutRes
    abstract fun getHeaderLayout(): Int

    val renderType: Class<T>
        get() {
//            val type = javaClass.genericSuperclass as ParameterizedType
//            return type.actualTypeArguments[0].javaClass as Class<T>
            return javaClass.genericSuperclass as Class<T>
        }
}
