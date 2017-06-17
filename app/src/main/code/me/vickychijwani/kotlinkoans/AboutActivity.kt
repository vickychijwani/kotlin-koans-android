package me.vickychijwani.kotlinkoans

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.activity_about.*
import me.vickychijwani.kotlinkoans.util.browse


class AboutActivity : AppCompatActivity() {

    private val URL_GITHUB_CONTRIBUTING = "https://github.com/vickychijwani/kotlin-koans-android/issues"
    private val URL_MY_WEBSITE = "http://vickychijwani.me"
    private val URL_TWITTER_PROFILE = "https://twitter.com/vickychijwani"
    private val URL_GITHUB_REPO = "https://github.com/vickychijwani/kotlin-koans-android"
    private val URL_GITHUB_PROFILE = "https://github.com/vickychijwani"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        about_version.text = getAppVersion(this)
        about_open_source_libs.setOnClickListener {
            startActivity(Intent(this, OpenSourceLibsActivity::class.java))
        }
        about_me.setOnClickListener { browse(this, URL_GITHUB_PROFILE) }
        about_github.setOnClickListener { browse(this, URL_GITHUB_REPO) }
        about_twitter.setOnClickListener { browse(this, URL_TWITTER_PROFILE) }
        about_website.setOnClickListener { browse(this, URL_MY_WEBSITE) }
        about_report_bugs.setOnClickListener { browse(this, URL_GITHUB_CONTRIBUTING) }
        about_play_store.setOnClickListener { openPlayStore(this) }
        about_email_developer.setOnClickListener { emailDeveloper(this) }
    }

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

}
