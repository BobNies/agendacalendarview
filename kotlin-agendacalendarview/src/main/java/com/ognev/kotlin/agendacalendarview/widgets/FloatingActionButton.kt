package com.ognev.kotlin.agendacalendarview.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Floating action button helping to scroll back to the current date.
 */
class FloatingActionButton : FloatingActionButton {

    var isVisible = true
        private set

    private val mInterpolator = AccelerateDecelerateInterpolator()

    // region Constructors

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    // endregion

    // region Overrides

    override
    fun show() {
        show(true)
    }

    override
    fun hide() {
        hide(true)
    }

    // endregion

    // region Public methods

    fun show(animate: Boolean) {
        toggle(true, animate, false)
    }

    fun hide(animate: Boolean) {
        toggle(false, animate, false)
    }

    // endregion

    // region Private methods

    private fun toggle(visible: Boolean, animate: Boolean, force: Boolean) {
        if (isVisible != visible || force) {
            isVisible = visible
            val height = height
            if (height == 0 && !force) {
                val vto = viewTreeObserver
                if (vto.isAlive) {
                    vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                        override
                        fun onPreDraw(): Boolean {
                            val currentVto = viewTreeObserver
                            if (currentVto.isAlive) {
                                currentVto.removeOnPreDrawListener(this)
                            }
                            toggle(visible, animate, true)
                            return true
                        }
                    })
                    return
                }
            }
            val translationY: Float = if (visible) 0f else height + marginBottom as Float
            if (animate) {
                animate().setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(translationY)
            } else {
                setTranslationY(translationY)
            }
        }
    }

    private val marginBottom: Int
        get() {
            var marginBottom = 0
            val layoutParams = layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                marginBottom = layoutParams.bottomMargin
            }
            return marginBottom
        }

    companion object {
        private const val TRANSLATE_DURATION_MILLIS: Long = 200
    }

    // endregion
}
