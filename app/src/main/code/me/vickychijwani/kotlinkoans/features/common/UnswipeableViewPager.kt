package me.vickychijwani.kotlinkoans.features.common

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class UnswipeableViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {

    constructor(context: Context) : this(context, null)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

}
