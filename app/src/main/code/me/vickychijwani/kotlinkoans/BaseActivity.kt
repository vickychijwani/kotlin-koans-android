package me.vickychijwani.kotlinkoans

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.tsengvn.typekit.TypekitContextWrapper


abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase))
    }

}
