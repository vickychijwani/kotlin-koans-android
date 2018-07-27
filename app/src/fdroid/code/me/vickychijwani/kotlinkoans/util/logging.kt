package me.vickychijwani.kotlinkoans.util

import android.util.Log
import me.vickychijwani.kotlinkoans.BuildConfig

inline fun logDebug(tag: String = "Unknown", msg: () -> String) {
    Log.d(tag, msg())
}

inline fun logInfo(tag: String = "Unknown", msg: () -> String) {
    // this also writes to Android logcat
    // Crashlytics.log(Log.INFO, tag, msg())
}

inline fun logWarn(tag: String = "Unknown", msg: () -> String) {
    // this also writes to Android logcat
    // Crashlytics.log(Log.WARN, tag, msg())
}

inline fun logError(tag: String = "Unknown", msg: () -> String) {
    // this also writes to Android logcat
    // Crashlytics.log(Log.ERROR, tag, msg())
}

fun logException(error: Throwable, tag: String = "Unknown") {
    // Crashlytics.log(Log.ERROR, tag, Log.getStackTraceString(error))
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
