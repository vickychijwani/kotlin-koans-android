package me.vickychijwani.kotlinkoans.network

import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

class DebugHttpClientFactory : ProductionHttpClientFactory() {

    override fun create(cacheDir: File?): OkHttpClient {
        return super.create(cacheDir).newBuilder()
                // allow inspecting network requests with Chrome DevTools
                .addNetworkInterceptor(StethoInterceptor())
                // log requests and responses
                .addInterceptor(HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
    }

}
