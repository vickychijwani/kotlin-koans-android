package me.vickychijwani.kotlinkoans

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KoanRepository {

    private val TAG = KoanRepository::class.java.simpleName
    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://try.kotlinlang.org/")
            .client(KotlinKoansApplication.getInstance().getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    private val api = retrofit.create(KotlinKoansApiService::class.java)

    fun listKoans(callback: (KoanFolders) -> Unit) {
        api.listKoans().enqueue(object : Callback<KoanFolders> {
            override fun onResponse(call: Call<KoanFolders>, response: Response<KoanFolders>) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    Log.e(TAG, "Failed to fetch")
                }
            }

            override fun onFailure(call: Call<KoanFolders>, t: Throwable) {
                Log.e(TAG, Log.getStackTraceString(t))
            }
        })
    }

    fun getKoan(id: String, callback: (Koan) -> Unit) {
        api.getKoan(id).enqueue(object : Callback<Koan> {
            override fun onResponse(call: Call<Koan>, response: Response<Koan>) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    Log.e(TAG, "Failed to fetch")
                }
            }

            override fun onFailure(call: Call<Koan>, t: Throwable) {
                Log.e(TAG, Log.getStackTraceString(t))
            }
        })
    }

    fun runKoan(koan: Koan, callback: (KoanRunResults) -> Unit) {
        val (modifiableFiles, readOnlyFiles) = koan.files.partition { it.modifiable }
        val modifiableFile: KoanFile = modifiableFiles[0]
        val runInfo = KoanRunInfo(
                id = koan.id,
                name = koan.name,
                files = listOf(modifiableFile),
                readOnlyFileNames = readOnlyFiles.map { it.name }
        )
        val runInfoJson = Gson().toJson(runInfo)
        api.runKoan(modifiableFile.name, runInfoJson).enqueue(object : Callback<KoanRunResults> {
            override fun onResponse(call: Call<KoanRunResults>, response: Response<KoanRunResults>) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    Log.e(TAG, "Failed to run koan")
                }
            }

            override fun onFailure(call: Call<KoanRunResults>, t: Throwable) {
                Log.e(TAG, Log.getStackTraceString(t))
            }

        })
    }

}
