package me.vickychijwani.kotlinkoans.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.getkeepsafe.taptargetview.TapTarget
import me.vickychijwani.kotlinkoans.R


fun emailDeveloper(activity: Activity) {
    val emailSubject = activity.getString(R.string.email_subject,
            activity.getString(R.string.app_name))
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:") // only email apps should handle this
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("vickychijwani@gmail.com"))
    intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)

    var body = "App version: " + getAppVersion(activity) + "\n"
    body += "Android API version: " + Build.VERSION.SDK_INT + "\n"
    body += "\n"
    intent.putExtra(Intent.EXTRA_TEXT, body)

    if (intent.resolveActivity(activity.packageManager) != null) {
        activity.startActivity(intent)
    } else {
        Toast.makeText(activity, R.string.intent_no_apps, Toast.LENGTH_LONG)
                .show()
    }
}

fun getAppVersion(context: Context): String {
    try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        if (packageInfo != null) {
            return packageInfo.versionName
        } else {
            return context.getString(R.string.version_unknown)
        }
    } catch (e: PackageManager.NameNotFoundException) {
        Crashlytics.logException(RuntimeException("Failed to get package info, " + "see previous exception for details", e))
        return context.getString(R.string.version_unknown)
    }
}

fun TapTarget.styleWithDefaults(): TapTarget {
    return this
            .outerCircleColor(R.color.tip_background)
            .titleTextColor(R.color.text_primary)
            .titleTextSize(18)
            .descriptionTextColor(R.color.text_primary)
            .descriptionTextSize(15)
            .outerCircleAlpha(1f)
}

fun openPlayStore(context: Context) {
    val appPackageName = context.packageName
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appPackageName)))
    } catch (anfe: android.content.ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)))
    }
}
