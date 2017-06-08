package me.vickychijwani.kotlinkoans

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface KotlinKoansApiService {

    @GET("kotlinServer?type=loadHeaders")
    fun listKoans(): Call<List<KoanFolder>>

    @GET("kotlinServer?type=loadExample&ignoreCache=false")
    fun getKoan(@Query("publicId") id: String): Call<Koan>

}
