package me.vickychijwani.kotlinkoans.features.common

import android.content.Context
import android.graphics.Typeface
import android.support.design.widget.TabLayout
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView


class TabLayoutWithFonts : TabLayout {
    private var mTypeface: Typeface? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mTypeface = Typeface.createFromAsset(context.assets, "fonts/bold.ttf")
    }

    override fun addTab(tab: Tab, position: Int, setSelected: Boolean) {
        super.addTab(tab, position, setSelected)

        val mainView = getChildAt(0) as ViewGroup
        val tabView = mainView.getChildAt(tab.position) as ViewGroup

        val tabChildCount = tabView.childCount
        for (i in 0..tabChildCount - 1) {
            val tabViewChild = tabView.getChildAt(i)
            if (tabViewChild is TextView) {
                tabViewChild.setTypeface(mTypeface, Typeface.NORMAL)
            }
        }
    }

}
