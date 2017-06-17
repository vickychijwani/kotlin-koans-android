package me.vickychijwani.kotlinkoans.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

class DebugHttpClientFactory : ProductionHttpClientFactory() {

    override fun create(cacheDir: File?): OkHttpClient {
        return super.create(cacheDir).newBuilder()
                // log requests and responses
                .addInterceptor(HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
    }

}
