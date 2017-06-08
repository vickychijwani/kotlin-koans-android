package me.vickychijwani.kotlinkoans.network

import okhttp3.OkHttpClient
import java.io.File

interface HttpClientFactory {

    fun create(cacheDir: File?): OkHttpClient

}
