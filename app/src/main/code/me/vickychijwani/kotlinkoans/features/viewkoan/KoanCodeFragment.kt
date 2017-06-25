package me.vickychijwani.kotlinkoans.features.viewkoan

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.code_editor.*
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.data.KoanFile
import me.vickychijwani.kotlinkoans.features.common.getSizeDimen
import me.vickychijwani.kotlinkoans.util.getScreenWidth
import me.vickychijwani.kotlinkoans.util.waitForMeasurement

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        mFileIndex = arguments.getInt(KEY_FILE_INDEX)
        return inflater.inflate(R.layout.fragment_koan_code, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        code_editor.setupForEditing()   // assume it's editable, will be updated later
        code_editor.minHeight = getSizeDimen(context, R.dimen.code_editor_min_height)
        code_editor.waitForMeasurement {
            val editorHorizontalPadding = code_padding.paddingStart + code_padding.paddingEnd
            code_editor.minWidth = getScreenWidth(context) - editorHorizontalPadding
        }
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
        code_editor.setText(koanFile.contents)
        if (koanFile.modifiable) {
            code_editor.enableEditing()
        } else {
            code_editor.disableEditing()
        }
    }

    fun getUserCode(): KoanFile? {
        return if (mKoanFile.modifiable) {
            mKoanFile.copy(contents = code_editor.text.toString())
        } else {
            null
        }
    }

}
