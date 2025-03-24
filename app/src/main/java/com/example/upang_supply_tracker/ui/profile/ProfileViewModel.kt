package com.example.upang_supply_tracker.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.upang_supply_tracker.Services.CartService
import com.example.upang_supply_tracker.Services.ReservationService
import com.example.upang_supply_tracker.backend.ApiService
import com.example.upang_supply_tracker.backend.RetrofitClient
import com.example.upang_supply_tracker.dataclass.Student
import com.example.upang_supply_tracker.models.Reservation
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel : ViewModel() {

    private val _studentData = MutableLiveData<Student>()
    val studentData: LiveData<Student> = _studentData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _reservations = MutableLiveData<List<Reservation>>()
    val reservations: LiveData<List<Reservation>> = _reservations

    private val reservationService = ReservationService.getInstance()

    fun fetchStudentData() {
        _isLoading.value = true

        // Get student number from CartService
        val studentNumber = CartService.getInstance().getStudentNumber()

        if (studentNumber.isNullOrEmpty()) {
            _error.value = "Student number not found"
            _isLoading.value = false
            return
        }

        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        val call = apiService.getStudentProfile(studentNumber)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseString = response.body()?.string() ?: ""
                        val jsonResponse = JSONObject(responseString)

                        val success = jsonResponse.optBoolean("success", false)

                        if (success) {
                            val studentData = jsonResponse.getJSONObject("data")

                            val student = Student(
                                fullName = studentData.optString("FullName", ""),
                                studentNumber = studentData.optString("StudentNumber", ""),
                                password = "", // We don't want to store the password
                                departmentID = studentData.optString("DepartmentID", ""),
                                courseID = studentData.optString("CourseID", "")
                            )

                            _studentData.value = student

                            // Now fetch reservations
                            fetchReservations(student.studentNumber)
                        } else {
                            _error.value = jsonResponse.optString("message", "Failed to fetch profile")
                            _isLoading.value = false
                        }
                    } catch (e: Exception) {
                        _error.value = "Error parsing response: ${e.message}"
                        _isLoading.value = false
                    }
                } else {
                    _error.value = "Server error: ${response.code()}"
                    _isLoading.value = false
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Network error: ${t.message}"
            }
        })
    }

    fun fetchReservations(studentNumber: String) {
        reservationService.getReservations(
            studentNumber = studentNumber,
            onSuccess = { reservationsList ->
                _reservations.value = reservationsList
                _isLoading.value = false
            },
            onError = { errorMessage ->
                _error.value = errorMessage
                _isLoading.value = false
            }
        )
    }
}