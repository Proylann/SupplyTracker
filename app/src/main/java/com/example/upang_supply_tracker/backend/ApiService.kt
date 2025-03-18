package com.example.upang_supply_tracker.backend

import com.example.upang_supply_tracker.dataclass.BookResponse
import com.example.upang_supply_tracker.dataclass.Course
import com.example.upang_supply_tracker.dataclass.Department
import com.example.upang_supply_tracker.dataclass.Student
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("register.php")
    fun createStudent(@Body student: Student): Call<ResponseBody>
    @Headers("Content-Type: application/json")
    @POST("login.php")
    fun loginUser(@Body student: Student): Call<ResponseBody>

    @GET("mobile_fetch_department.php")
    fun getDepartments(): Call<List<Department>>

    @GET("mobile_fetch_course.php")
    fun getCourse(@Query("department_id") departmentId: String): Call<List<Course>>

    @GET("mobile_fetch_books.php")
    fun getBooks(): Call<BookResponse>
}
