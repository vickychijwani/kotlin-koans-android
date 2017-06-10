package me.vickychijwani.kotlinkoans.network

import android.os.Build
import android.os.StatFs
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

open class ProductionHttpClientFactory : HttpClientFactory {

    /**
     * @param cacheDir - directory for the HTTP cache, disabled if null
     * *
     * @return an HTTP client intended for production use
     */
    override fun create(cacheDir: File?): OkHttpClient {
        val builder = OkHttpClient.Builder()
        if (cacheDir != null) {
            val size = calculateDiskCacheSize(cacheDir)
            builder.cache(Cache(cacheDir, size))
        }
        return builder
                .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .build()
    }

    companion object {

        private val MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024     // in bytes
        private val MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024    // in bytes

        private val CONNECT_TIMEOUT = 20
        private val READ_TIMEOUT = 30
        private val WRITE_TIMEOUT = 5 * 60    // for file uploads

        private fun calculateDiskCacheSize(dir: File): Long {
            var size = MIN_DISK_CACHE_SIZE.toLong()
            try {
                val statFs = StatFs(dir.absolutePath)
                val available: Long
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    available = statFs.blockCountLong * statFs.blockSizeLong
                } else {
                    // checked at runtime

                    available = (statFs.blockCount * statFs.blockSize).toLong()
                }
                // Target 2% of the total space.
                size = available / 50
            } catch (ignored: IllegalArgumentException) {
            }

            // Bound inside min/max size for disk cache.
            return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE.toLong()), MIN_DISK_CACHE_SIZE.toLong())
        }
    }

}
