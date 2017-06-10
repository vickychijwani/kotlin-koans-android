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
import me.vickychijwani.kotlinkoans.Koan
import me.vickychijwani.kotlinkoans.KoanFile
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.features.common.WebViewFragment
import java.util.*

class KoanCodeFragment(): LifecycleFragment(), Observer<Koan> {

    companion object {
        val KEY_FILE_ID = "key:file-id"
        fun newInstance(fileId: Int): KoanCodeFragment {
            val fragment = KoanCodeFragment()
            fragment.arguments = Bundle()
            fragment.arguments.putInt(KEY_FILE_ID, fileId)
            return fragment
        }
    }

    private val TAG = KoanCodeFragment::class.java.simpleName

    private var mFileId: Int = -1
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
        val view = inflater.inflate(R.layout.fragment_koan_description, container, false)

        mFileId = arguments.getInt(KEY_FILE_ID)

        val vm = ViewModelProviders.of(activity).get(KoanViewModel::class.java)
        vm.liveData.observe(activity as LifecycleOwner, this@KoanCodeFragment)

        return view
    }

    fun initWebView(isModifiable: Boolean) {
        if (isModifiable) {
            mWebViewFragment = WebViewFragment.newInstance("file:///android_asset/koan-code-editor.html")
        } else {
            mUserCodeObservable.notifyObservers(null)  // let observers know that there will be no more updates so they can unsubscribe
            mWebViewFragment = WebViewFragment.newInstance("file:///android_asset/koan-code-viewer.html")
        }
        // at this point the fragment must exist
        val webViewFragment = mWebViewFragment!!
        webViewFragment.setOnWebViewCreatedListener(object : WebViewFragment.OnWebViewCreatedListener {
            override fun onWebViewCreated() {
                webViewFragment.setJSInterface(object : Any() {
                    @JavascriptInterface
                    fun getCode(): String {
                        return mKoanFile.contents
                    }

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
                .add(R.id.web_view_container, mWebViewFragment)
                .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val vm = ViewModelProviders.of(activity).get(KoanViewModel::class.java)
        vm.liveData.removeObserver(this@KoanCodeFragment)
    }

    override fun onChanged(koan: Koan?) {
        if (koan?.files != null && mFileId < koan.files.size) {
            mKoanFile = koan.files[mFileId]
            showCode()
        }
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

    private fun showCode() {
        val koanFile = mKoanFile
        if (mWebViewFragment == null) {
            initWebView(koanFile.modifiable)
        } else {
            mWebViewFragment!!.evaluateJavascript("update()")
        }
    }

}
