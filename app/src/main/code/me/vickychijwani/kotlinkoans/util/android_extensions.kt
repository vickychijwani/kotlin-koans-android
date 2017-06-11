package me.vickychijwani.kotlinkoans.util

import android.support.design.widget.BottomSheetBehavior

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
