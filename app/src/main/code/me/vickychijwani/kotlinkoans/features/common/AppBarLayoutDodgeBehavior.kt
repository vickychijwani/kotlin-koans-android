package me.vickychijwani.kotlinkoans.features.common

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.util.AttributeSet
import android.view.View

class AppBarLayoutDodgeBehavior(ctx: Context, attrs: AttributeSet)
    : CoordinatorLayout.Behavior<AppBarLayout>(ctx, attrs) {

    private var animating = false

    override fun layoutDependsOn(parent: CoordinatorLayout, appbar: AppBarLayout, dependency: View): Boolean {
        val lp = dependency.layoutParams as? CoordinatorLayout.LayoutParams
        return if (lp != null) lp.behavior is BottomSheetBehavior else false
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, fab: AppBarLayout, dependency: View): Boolean {
        val appbarHeight = fab.measuredHeight
        if (!animating && dependency.y <= appbarHeight) {
            ViewCompat.animate(fab).withLayer()
                    .translationY(-appbarHeight.toFloat())
                    .setListener(object : ViewPropertyAnimatorListener {
                        override fun onAnimationEnd(view: View?) { animating = false }
                        override fun onAnimationCancel(view: View?) { animating = false }
                        override fun onAnimationStart(view: View?) { animating = true }
                    })
                    .start()
        } else if (!animating && dependency.y > appbarHeight) {
            ViewCompat.animate(fab).withLayer()
                    .translationY(0f)
                    .setListener(object : ViewPropertyAnimatorListener {
                        override fun onAnimationEnd(view: View?) { animating = false }
                        override fun onAnimationCancel(view: View?) { animating = false }
                        override fun onAnimationStart(view: View?) { animating = true }
                    })
                    .start()
        }
        return false
    }

}
