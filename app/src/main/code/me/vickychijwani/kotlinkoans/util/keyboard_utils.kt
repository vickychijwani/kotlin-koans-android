package me.vickychijwani.kotlinkoans.util

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.IBinder
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


/**
 * Show the soft keyboard.
 * @param activity the current activity
 */
fun showKeyboard(activity: Activity?) {
    if (activity == null) return
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

/**
 * Hide the soft keyboard.
 * @param activity the current activity
 */
fun hideKeyboard(activity: Activity?) {
    if (activity == null) return
    val view = activity.currentFocus
    if (view != null) {
        hideKeyboard(activity, view.windowToken)
    }
}

/**
 * Focus the given view and show the soft keyboard.
 * @param activity the current activity
 * *
 * @param view the view to focus
 */
fun focusAndShowKeyboard(activity: Activity?, view: View) {
    if (activity == null) return
    if (view.isFocusable) {
        view.requestFocus()
    }
    if (view is EditText) {
        showKeyboard(activity)
    }
}

/**
 * Clear focus from the current view and hide the soft keyboard.
 * @param activity the current activity
 */
fun defocusAndHideKeyboard(activity: Activity?) {
    if (activity == null) return
    val view = activity.currentFocus
    if (view != null) {
        view.clearFocus()
        hideKeyboard(activity, view.windowToken)
    }
}

// courtesy http://stackoverflow.com/a/18992807/504611
fun addKeyboardVisibilityChangedListener(activity: Activity,
        listener: (visible: Boolean, contentHeight: Int) -> Unit) {
    val activityRootView = (activity.findViewById(android.R.id.content) as ViewGroup)
            .getChildAt(0)
    val viewTreeObserver = activityRootView.viewTreeObserver
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        private val MINIMUM_KEYBOARD_SIZE_DP = 100
        private val r = Rect()

        override fun onGlobalLayout() {
            val minimumKeyboardHeight = MINIMUM_KEYBOARD_SIZE_DP.dp
            activityRootView.getWindowVisibleDisplayFrame(r)
            val contentHeight = r.bottom - r.top
            val heightDiff = activityRootView.rootView.height - contentHeight
            val isVisible = heightDiff >= minimumKeyboardHeight
            listener(isVisible, contentHeight)
        }
    })
}


// private methods
private fun hideKeyboard(activity: Activity?, windowToken: IBinder?) {
    if (activity == null) return
    val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}
