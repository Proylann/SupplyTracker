package com.example.upang_supply_tracker

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("create.php")
    fun createStudent(@Body student: Student): Call<ResponseBody>


    @GET("read.php")
    fun getStudent(@Body student: Student):  Call<ResponseBody>

}
