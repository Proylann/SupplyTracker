package com.example.upang_supply_tracker.backend


import com.example.upang_supply_tracker.dataclass.BookResponse
import com.example.upang_supply_tracker.dataclass.Course
import com.example.upang_supply_tracker.dataclass.Department
import com.example.upang_supply_tracker.dataclass.Student
import com.example.upang_supply_tracker.models.ModuleResponse
import com.example.upang_supply_tracker.models.ReservationRequest
import com.example.upang_supply_tracker.models.ReservationResponse
import com.example.upang_supply_tracker.models.UniformResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
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

    @GET("getStudent.php")
    fun getStudentProfile(@Query("student_number") studentNumber: String): Call<ResponseBody>

    @GET("mobile_fetch_department.php")
    fun getDepartments(): Call<List<Department>>

    @GET("mobile_fetch_course.php")
    fun getCourse(@Query("department_id") departmentId: String): Call<List<Course>>

    @GET("mobile_fetch_books.php")
    fun getBooks(): Call<BookResponse>

    @GET("mobile_fetch_uniform.php")
    fun getUniforms(): Call<UniformResponse>

    @GET("mobile_fetch_uniforms.php")
    fun searchUniforms(@Query("search") query: String): Call<UniformResponse>

    @GET("mobile_fetch_modules.php")
    fun getModules(): Call<ModuleResponse>

    @GET("mobile_fetch_modules.php")
    fun searchModules(@Query("search") query: String): Call<ModuleResponse>

    @POST("setReservation.php")
    suspend fun submitReservation(@Body request: ReservationRequest): Response<ReservationResponse>
}