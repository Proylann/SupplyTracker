package com.example.upang_supply_tracker.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.upang_supply_tracker.BottomNavigation
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.Services.CartService
import com.example.upang_supply_tracker.backend.ApiService
import com.example.upang_supply_tracker.backend.RetrofitClient
import com.example.upang_supply_tracker.dataclass.Student
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private lateinit var studentNumberEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize CartService with application context
        CartService.initialize(applicationContext)

        // Initialize UI elements
        studentNumberEditText = findViewById(R.id.StudentNumber)
        passwordEditText = findViewById(R.id.Password)
        submitButton = findViewById(R.id.btnSubmit)

        // Set click listener for submit button
        submitButton.setOnClickListener {
            validateAndLogin()
        }
    }

    private fun validateAndLogin() {
        val studentNumber = studentNumberEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Basic validation
        if (studentNumber.isEmpty()) {
            studentNumberEditText.error = "Student Number is required"
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return
        }

        // Create Student object for login
        val loginStudent = Student(
            fullName = "", // Not needed for login
            studentNumber = studentNumber,
            password = password,
            departmentID = "",
            courseID = ""
        )

        // Attempt login
        loginUser(loginStudent)
    }

    private fun loginUser(loginStudent: Student) {
        submitButton.isEnabled = false

        // Get API service instance
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        // Make API call using loginUser method
        val call = apiService.loginUser(loginStudent)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                submitButton.isEnabled = true

                if (response.isSuccessful) {
                    try {
                        val responseString = response.body()?.string() ?: ""
                        val jsonResponse = JSONObject(responseString)

                        val success = jsonResponse.optBoolean("success", false)
                        val message = jsonResponse.optString("message", "")

                        if (success) {
                            // Save student number in CartService
                            CartService.getInstance().saveStudentNumber(loginStudent.studentNumber)

                            Toast.makeText(
                                this@Login,
                                "Login successful!",
                                Toast.LENGTH_LONG
                            ).show()

                            // Navigate to main activity
                            val intent = Intent(this@Login, BottomNavigation::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@Login,
                                message.ifEmpty { "Invalid credentials" },
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e("LOGIN_ERROR", "Parse error: ${e.message}")
                        Toast.makeText(
                            this@Login,
                            "Error processing response. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Log.e("LOGIN_ERROR", "Error: ${response.code()}")
                    Toast.makeText(
                        this@Login,
                        "Login failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                submitButton.isEnabled = true
                Log.e("LOGIN_ERROR", "Failure: ${t.message}")
                Toast.makeText(
                    this@Login,
                    "Network error. Please check your connection.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}