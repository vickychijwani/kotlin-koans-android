package me.vickychijwani.kotlinkoans.features.viewkoan

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import me.vickychijwani.kotlinkoans.Koan
import me.vickychijwani.kotlinkoans.MainActivity
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.features.common.WebViewFragment
import me.vickychijwani.kotlinkoans.util.browse
import me.vickychijwani.kotlinkoans.util.debug
import me.vickychijwani.kotlinkoans.util.reportNonFatal


class KoanDescriptionFragment(): LifecycleFragment(), Observer<Koan> {

    companion object {
        fun newInstance(): KoanDescriptionFragment {
            val fragment = KoanDescriptionFragment()
            fragment.arguments = Bundle.EMPTY
            return fragment
        }
    }

    private var mWebViewFragment: WebViewFragment? = null
    private var mKoan: Koan? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_koan_description, container, false)
        mWebViewFragment = WebViewFragment.newInstance("file:///android_asset/koan-description.html")
        mWebViewFragment?.setOnWebViewCreatedListener(object : WebViewFragment.OnWebViewCreatedListener {
            override fun onWebViewCreated() {
                mWebViewFragment?.setJSInterface(object : Any() {
                    @JavascriptInterface
                    fun getDescription(): String {
                        val koan = mKoan
                        return (koan?.descriptionHtml ?: "")
                    }
                }, "KOAN")
                mWebViewFragment?.setWebViewClient(object : WebViewFragment.DefaultWebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        showKoan()
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        when {
                            url.startsWith("http://") || url.startsWith("https://") ->
                                browse(this@KoanDescriptionFragment, url)
                            // switch to tab containing this Kotlin file, if it exists
                            url.startsWith("file://") && url.endsWith(".kt") -> {
                                val activity = this@KoanDescriptionFragment.activity
                                if (activity is MainActivity) {
                                    activity.switchToFile(url.split('/').last())
                                }
                            }
                            else -> reportNonFatal(UnknownUrlTypeException(url))
                        }
                        return true
                    }
                })

                val vm = ViewModelProviders.of(activity).get(KoanViewModel::class.java)
                vm.liveData.observe(activity as LifecycleOwner, this@KoanDescriptionFragment)
            }
        })
        childFragmentManager
                .beginTransaction()
                .replace(R.id.web_view_container, mWebViewFragment)
                .commit()

        return view
    }

    override fun onChanged(koan: Koan?) {
        mKoan = koan
        showKoan()
    }

    fun showKoan() {
        debug { "Updating view, current koan is ${mKoan?.name}" }
        mWebViewFragment?.evaluateJavascript("update()")
    }

    private class UnknownUrlTypeException(url: String)
        : RuntimeException("Unknown URL type: $url")

}
