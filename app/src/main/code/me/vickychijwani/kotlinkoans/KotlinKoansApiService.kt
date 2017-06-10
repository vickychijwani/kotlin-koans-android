package me.vickychijwani.kotlinkoans

import retrofit2.Call
import retrofit2.http.*

interface KotlinKoansApiService {

    @GET("kotlinServer?type=loadHeaders")
    fun listKoans()
            : Call<KoanFolders>

    @GET("kotlinServer?type=loadExample&ignoreCache=false")
    fun getKoan(@Query("publicId") id: String)
            : Call<Koan>

    @FormUrlEncoded
    @POST("kotlinServer?type=run&runConf=junit")
    fun runKoan(@Field("filename") filename: String, @Field("project") runInfo: String)
            : Call<KoanRunResults>

}
