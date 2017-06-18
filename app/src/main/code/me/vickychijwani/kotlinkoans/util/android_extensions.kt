package me.vickychijwani.kotlinkoans.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewTreeObserver

fun browse(activity: Activity, url: String) {
    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

fun browse(fragment: Fragment, url: String) {
    fragment.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

fun BottomSheetBehavior<*>.isExpanded() = (state == BottomSheetBehavior.STATE_EXPANDED)
fun BottomSheetBehavior<*>.isCollapsed() = (state == BottomSheetBehavior.STATE_COLLAPSED)
fun BottomSheetBehavior<*>.expand() { state = BottomSheetBehavior.STATE_EXPANDED }
fun BottomSheetBehavior<*>.collapse() { state = BottomSheetBehavior.STATE_COLLAPSED }

fun BottomSheetBehavior<*>.toggleState() {
    if (this.state == BottomSheetBehavior.STATE_COLLAPSED) {
        this.state = BottomSheetBehavior.STATE_EXPANDED
    } else {
        this.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }
val View.isVisible: Boolean
    get() = visibility == View.VISIBLE

// courtesy https://antonioleiva.com/kotlin-ongloballayoutlistener/
inline fun <T: View> T.waitForMeasurement(crossinline callback: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                callback()
            }
        }
    })
}

fun getScreenWidth(context: Context): Int {
    return context.resources.displayMetrics.widthPixels
}

fun getScreenHeight(context: Context): Int {
    return context.resources.displayMetrics.heightPixels
}
