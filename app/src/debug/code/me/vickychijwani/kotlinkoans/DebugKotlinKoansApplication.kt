package me.vickychijwani.kotlinkoans

import android.app.Activity
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import io.palaima.debugdrawer.DebugDrawer
import io.palaima.debugdrawer.commons.BuildModule
import io.palaima.debugdrawer.commons.DeviceModule
import io.palaima.debugdrawer.commons.SettingsModule
import io.palaima.debugdrawer.okhttp3.OkHttp3Module
import io.palaima.debugdrawer.scalpel.ScalpelModule
import me.vickychijwani.kotlinkoans.network.DebugHttpClientFactory

class DebugKotlinKoansApplication : KotlinKoansApplication() {

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }

        // auto-detect Activity memory leaks!
        LeakCanary.install(this)

        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .build())
    }

    override fun initOkHttpClient() {
        val cacheDir = createCacheDir(this)
        mOkHttpClient = DebugHttpClientFactory().create(cacheDir)
    }

    override fun addDebugDrawer(activity: Activity) {
        DebugDrawer.Builder(activity).modules(
                ScalpelModule(activity),
                OkHttp3Module(mOkHttpClient),
                DeviceModule(activity),
                BuildModule(activity),
                SettingsModule(activity)
        ).build()
    }

}
