package me.vickychijwani.kotlinkoans.features.viewkoan

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import me.vickychijwani.kotlinkoans.Koan
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.features.common.WebViewFragment


class KoanDescriptionFragment(): LifecycleFragment(), Observer<Koan> {

    companion object {
        fun newInstance(): KoanDescriptionFragment {
            val fragment = KoanDescriptionFragment()
            fragment.arguments = Bundle.EMPTY
            return fragment
        }
    }

    private val TAG = KoanDescriptionFragment::class.java.simpleName

    private lateinit var mWebViewFragment: WebViewFragment
    private var mKoan: Koan? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_koan_description, container, false)
        mWebViewFragment = WebViewFragment.newInstance("file:///android_asset/koan-description.html")
        mWebViewFragment.setOnWebViewCreatedListener(object : WebViewFragment.OnWebViewCreatedListener {
            override fun onWebViewCreated() {
                mWebViewFragment.setJSInterface(object : Any() {
                    @JavascriptInterface
                    fun getDescription(): String {
                        val koan = mKoan
                        return (koan?.descriptionHtml ?: "")
                    }
                }, "KOAN")
                mWebViewFragment.setWebViewClient(object : WebViewFragment.DefaultWebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        showKoan()
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        // launch links in external browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }
                })

                val vm = ViewModelProviders.of(activity).get(ViewKoanViewModel::class.java)
                vm.liveData.observe(activity as LifecycleOwner, this@KoanDescriptionFragment)
            }
        })
        childFragmentManager
                .beginTransaction()
                .add(R.id.web_view_container, mWebViewFragment)
                .commit()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val vm = ViewModelProviders.of(activity).get(ViewKoanViewModel::class.java)
        vm.liveData.removeObserver(this@KoanDescriptionFragment)
    }

    override fun onChanged(koan: Koan?) {
        mKoan = koan
        showKoan()
    }

    fun showKoan() {
        Log.d(TAG, "Updating view, current koan is ${mKoan?.name}")
        mWebViewFragment.evaluateJavascript("update()")
    }

}
