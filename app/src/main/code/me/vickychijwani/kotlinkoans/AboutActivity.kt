package me.vickychijwani.kotlinkoans

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_about.*
import me.vickychijwani.kotlinkoans.util.browse
import me.vickychijwani.kotlinkoans.util.emailDeveloper
import me.vickychijwani.kotlinkoans.util.getAppVersion
import me.vickychijwani.kotlinkoans.util.openPlayStore


class AboutActivity : BaseActivity() {

    private val URL_KOTLIN_KOANS_JETBRAINS = "https://github.com/Kotlin/kotlin-koans"
    private val URL_KOTLIN_RESOURCES = "https://developer.android.com/kotlin/resources.html"
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
        about_kotlin_koans_jetbrains.setOnClickListener { browse(this, URL_KOTLIN_KOANS_JETBRAINS) }
        about_kotlin_resources.setOnClickListener { browse(this, URL_KOTLIN_RESOURCES) }
        about_me.setOnClickListener { browse(this, URL_GITHUB_PROFILE) }
        about_github.setOnClickListener { browse(this, URL_GITHUB_REPO) }
        about_twitter.setOnClickListener { browse(this, URL_TWITTER_PROFILE) }
        about_website.setOnClickListener { browse(this, URL_MY_WEBSITE) }
        about_report_bugs.setOnClickListener { browse(this, URL_GITHUB_CONTRIBUTING) }
        about_play_store.setOnClickListener { openPlayStore(this) }
        about_email_developer.setOnClickListener { emailDeveloper(this) }
    }

}
