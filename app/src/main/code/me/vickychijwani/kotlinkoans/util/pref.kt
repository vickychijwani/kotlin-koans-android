package me.vickychijwani.kotlinkoans.util

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.BoolRes
import android.support.annotation.IntegerRes
import android.support.annotation.StringRes

object Prefs {

    private var mPrefs: SharedPreferences? = null

    fun getFileName(context: Context): String {
        return "${context.packageName}.prefs"
    }

    fun with(context: Context): SharedPreferences {
        if (mPrefs == null) {
            val appContext = context.applicationContext
            if (appContext != null) {
                mPrefs = appContext.getSharedPreferences(getFileName(appContext),
                        Context.MODE_PRIVATE)
            } else {
                throw IllegalArgumentException("context.getApplicationContext() returned null")
            }
        }
        return mPrefs!!
    }

}

// extension functions to read a preference using a string resource as the key
fun SharedPreferences.getBoolean(ctx: Context, @StringRes prefKeyRes: Int, @BoolRes defValueRes: Int)
        : Boolean = getBoolean(ctx.getString(prefKeyRes), ctx.resources.getBoolean(defValueRes))

fun SharedPreferences.getInt(ctx: Context, @StringRes prefKeyRes: Int, @IntegerRes defValueRes: Int)
        : Int = getInt(ctx.getString(prefKeyRes), ctx.resources.getInteger(defValueRes))
