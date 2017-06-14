package me.vickychijwani.kotlinkoans.util

import android.util.Log
import com.crashlytics.android.Crashlytics
import me.vickychijwani.kotlinkoans.BuildConfig

inline fun debug(tag: String = "Unknown", msg: () -> String) {
    Log.d(tag, msg())
}

inline fun info(tag: String = "Unknown", msg: () -> String) {
    // this also writes to Android logcat
    Crashlytics.log(Log.INFO, tag, msg())
}

inline fun warn(tag: String = "Unknown", msg: () -> String) {
    // this also writes to Android logcat
    Crashlytics.log(Log.WARN, tag, msg())
}

inline fun error(tag: String = "Unknown", msg: () -> String) {
    // this also writes to Android logcat
    Crashlytics.log(Log.ERROR, tag, msg())
}

// didn't name this function "assert" to avoid clashing with built-in assert()
inline fun crashUnless(failureMessage: String = "Assertion failed! See stack trace",
                       value: () -> Boolean) {
    // check assertions only in debug builds
    if (BuildConfig.DEBUG && !value()) {
        throw AssertionError(failureMessage)
    }
}

fun reportNonFatal(error: Throwable) {
    Log.e("Non Fatal", Log.getStackTraceString(error))
    Crashlytics.logException(error)
}
