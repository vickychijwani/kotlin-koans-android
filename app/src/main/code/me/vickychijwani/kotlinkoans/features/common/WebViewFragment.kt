package me.vickychijwani.kotlinkoans.features.common

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.os.*
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.*
import android.webkit.*
import kotlinx.android.synthetic.main.fragment_web_view.*
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.util.logDebug


class WebViewFragment : Fragment() {

    companion object {
        val KEY_URL = "key:url"
        fun newInstance(url: String): WebViewFragment {
            val fragment = WebViewFragment()
            val args = Bundle()
            args.putString(KEY_URL, url)
            fragment.arguments = args
            return fragment
        }
    }

    interface OnWebViewCreatedListener {
        fun onWebViewCreated()
    }

    private var mUrl: String? = null
    private var mOnWebViewCreatedListener: OnWebViewCreatedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)
        // not using ButterKnife to ensure WebView is private
        mUrl = arguments!!.getString(KEY_URL)
        if (TextUtils.isEmpty(mUrl)) {
            throw IllegalArgumentException("Empty URL passed to WebViewFragment!")
        }
        logDebug { "Loading URL: " + mUrl!! }

        // enable remote debugging
        if (0 != (ApplicationInfo.FLAG_DEBUGGABLE and (activity?.applicationInfo?.flags ?: 0))
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        return view
    }

    fun setOnWebViewCreatedListener(listener: OnWebViewCreatedListener) {
        mOnWebViewCreatedListener = listener
    }

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface", "SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settings = web_view.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        web_view.webViewClient = DefaultWebViewClient()
        web_view.loadUrl(mUrl)

        mOnWebViewCreatedListener?.onWebViewCreated()
    }

    override fun onResume() {
        super.onResume()
        web_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        web_view.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // don't hold on to the listener (which could potentially be an Activity)
        mOnWebViewCreatedListener = null
        // destroy the WebView completely
        if (web_view != null) {
            // the WebView must be removed from the view hierarchy before calling destroy
            // to prevent a memory leak (#75)
            // See https://developer.android.com/reference/android/webkit/WebView.html#destroy%28%29
            (web_view.parent as ViewGroup).removeView(web_view)
            web_view.removeAllViews()
            web_view.destroy()
        }
    }

    // our custom methods
    val currentUrl: String?
        get() {
            if (web_view == null) {
                return null
            }
            var currentLoadedUrl: String? = web_view.originalUrl
            if (currentLoadedUrl == null) {
                currentLoadedUrl = mUrl
            }
            return currentLoadedUrl
        }

    val currentTitle: String?
        get() {
            if (web_view == null) {
                return null
            }
            return web_view.title
        }

    fun evaluateJavascript(javascript: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            web_view.evaluateJavascript(javascript, null)
        } else {
            web_view.loadUrl("javascript:" + javascript)
        }
    }

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    fun setJSInterface(jsInterface: Any, name: String) {
        web_view.addJavascriptInterface(jsInterface, name)
    }

    fun setWebViewClient(webViewClient: DefaultWebViewClient) {
        web_view.webViewClient = webViewClient
    }

    fun setWebChromeClient(webChromeClient: DefaultWebChromeClient) {
        web_view.webChromeClient = webChromeClient
    }

    fun onBackPressed(): Boolean {
        if (web_view.canGoBack()) {
            web_view.goBack()
            return true
        }
        return false
    }

    open class DefaultWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return false
        }
    }

    class DefaultWebChromeClient : WebChromeClient()    // no-op

}
