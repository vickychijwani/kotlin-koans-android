package me.vickychijwani.kotlinkoans.features.settings

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import me.vickychijwani.kotlinkoans.KotlinKoansApplication
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.util.Prefs
import me.vickychijwani.kotlinkoans.util.getStringAsInt


class SettingsActivity : Activity(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        val RESULT_CODE_SETTINGS_NOT_CHANGED = 100
        val RESULT_CODE_SETTINGS_CHANGED = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        fragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        setResult(RESULT_CODE_SETTINGS_NOT_CHANGED)
    }

    override fun onResume() {
        super.onResume()
        Prefs.with(this).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        Prefs.with(this).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        setResult(RESULT_CODE_SETTINGS_CHANGED)
    }

}


class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = Prefs.getFileName(
                KotlinKoansApplication.getInstance())
        addPreferencesFromResource(R.xml.preferences)
        if (activity != null) {
            updateIndentSizePrefSummary(Prefs.with(activity))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        updateIndentSizePrefSummary(Prefs.with(context))
    }

    override fun onResume() {
        super.onResume()
        Prefs.with(activity).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        Prefs.with(activity).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        if (key == getString(R.string.pref_indent_size)) {
            updateIndentSizePrefSummary(prefs)
        }
    }

    private fun updateIndentSizePrefSummary(prefs: SharedPreferences) {
        findPreference(getString(R.string.pref_indent_size))?.let { pref ->
            val indentSize = prefs.getStringAsInt(activity, R.string.pref_indent_size,
                    R.integer.pref_indent_size_default)
            pref.summary = "$indentSize spaces"
        }
    }

}
