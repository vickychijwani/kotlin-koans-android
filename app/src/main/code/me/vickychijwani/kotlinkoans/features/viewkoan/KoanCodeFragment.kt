package me.vickychijwani.kotlinkoans.features.viewkoan

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import me.vickychijwani.kotlinkoans.KoanFile
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.features.common.WebViewFragment
import java.util.*

class KoanCodeFragment(): LifecycleFragment(), Observer<KoanViewModel.KoanData> {

    companion object {
        val KEY_FILE_INDEX = "key:file-index"
        fun newInstance(fileIndex: Int): KoanCodeFragment {
            val fragment = KoanCodeFragment()
            fragment.arguments = Bundle()
            fragment.arguments.putInt(KEY_FILE_INDEX, fileIndex)
            return fragment
        }
    }

    private var mFileIndex: Int = -1
    private lateinit var mKoanFile: KoanFile
    private var mUserCodeObservable = object : Observable() {
        fun updateValue(arg: Any?) {
            setChanged()
            notifyObservers(arg)
        }
    }
    private var mWebViewFragment: WebViewFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        mFileIndex = arguments.getInt(KEY_FILE_INDEX)
        return inflater.inflate(R.layout.fragment_koan_description, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val vm = ViewModelProviders.of(activity).get(KoanViewModel::class.java)
        vm.liveData.observe(activity as LifecycleOwner, this@KoanCodeFragment)
    }

    override fun onChanged(koanData: KoanViewModel.KoanData?) {
        val koan = koanData?.koan
        if (koan?.files != null && mFileIndex < koan.files.size) {
            mKoanFile = koan.files[mFileIndex]
            showCode()
        }
    }

    private fun showCode() {
        val koanFile = mKoanFile
        if (mWebViewFragment == null) {
            initWebView(koanFile.modifiable)
        } else {
            mWebViewFragment!!.evaluateJavascript("update()")
        }
    }

    fun initWebView(isModifiable: Boolean) {
        mWebViewFragment = WebViewFragment.newInstance("file:///android_asset/koan-code.html")
        if (!isModifiable) {
            mUserCodeObservable.notifyObservers(null)  // let observers know that there will be no more updates so they can unsubscribe
        }
        // at this point the fragment must exist
        val webViewFragment = mWebViewFragment!!
        webViewFragment.setOnWebViewCreatedListener(object : WebViewFragment.OnWebViewCreatedListener {
            override fun onWebViewCreated() {
                webViewFragment.setJSInterface(object : Any() {
                    @JavascriptInterface
                    fun getCode(): String = mKoanFile.contents

                    @JavascriptInterface
                    fun isModifiable(): Boolean = isModifiable

                    @JavascriptInterface
                    fun setUserCode(code: String?) {
                        if (code != null) {
                            mUserCodeObservable.updateValue(mKoanFile.copy(contents = code))
                        }
                    }
                }, "KOAN")
                webViewFragment.setWebViewClient(object : WebViewFragment.DefaultWebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        showCode()
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        // launch links in external browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }
                })
            }
        })
        childFragmentManager
                .beginTransaction()
                .replace(R.id.web_view_container, mWebViewFragment)
                .commit()
    }

    fun getUserCodeObservable(): Observable {
        return mUserCodeObservable
    }

    // async call
    fun updateUserCode() {
        val webViewFragment = mWebViewFragment
        webViewFragment?.let {
            webViewFragment.evaluateJavascript("getUserCode()")
        }
    }

}
