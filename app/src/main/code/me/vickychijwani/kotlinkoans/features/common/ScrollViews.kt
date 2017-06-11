package me.vickychijwani.kotlinkoans.features.common

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import me.vickychijwani.kotlinkoans.R

class HorizontalScrollView(context: Context?, attrs: AttributeSet? = null) : HorizontalScrollView(context, attrs) {

    override fun getSolidColor(): Int {
        return ContextCompat.getColor(context, R.color.fading_edge)
    }

}

class NestedScrollView(context: Context?, attrs: AttributeSet? = null) : NestedScrollView(context, attrs) {

    override fun getSolidColor(): Int {
        return ContextCompat.getColor(context, R.color.fading_edge)
    }

}
