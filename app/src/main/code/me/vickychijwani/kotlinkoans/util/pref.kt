package me.vickychijwani.kotlinkoans.util

import android.content.Context
import android.content.SharedPreferences

object Prefs {

    private var mPrefs: SharedPreferences? = null

    fun with(context: Context): SharedPreferences {
        if (mPrefs == null) {
            val appContext = context.applicationContext
            if (appContext != null) {
                mPrefs = appContext.getSharedPreferences("${appContext.packageName}.prefs",
                        Context.MODE_PRIVATE)
            } else {
                throw IllegalArgumentException("context.getApplicationContext() returned null")
            }
        }
        return mPrefs!!
    }

}
