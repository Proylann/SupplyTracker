package com.example.upang_supply_tracker.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.upang_supply_tracker.BottomNavigation
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.Services.CartService
import com.example.upang_supply_tracker.Services.UserManager
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
    private lateinit var RegisterNav: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize CartService with application context
        CartService.initialize(applicationContext)

        // Initialize UI elements
        studentNumberEditText = findViewById(R.id.StudentNumber)
        passwordEditText = findViewById(R.id.Password)
        submitButton = findViewById(R.id.btnSubmit)
        RegisterNav = findViewById(R.id.RegisterNav)
        // Set click listener for submit button

        submitButton.setOnClickListener {
            validateAndLogin()
        }


        RegisterNav.setOnClickListener{
        val intent = Intent(this, Register::class.java)
            startActivity(intent)

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

        val apiService = RetrofitClient.instance.create(ApiService::class.java)
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
                            val studentData = jsonResponse.optJSONObject("student")
                            if (studentData != null) {
                                val fullName = studentData.optString("FullName", "")
                                val studentNumber = studentData.optString("StudentNumber", "")
                                val departmentID = studentData.optString("DepartmentID", "")
                                val courseID = studentData.optString("CourseID", "")

                                // Create a proper Student object with complete data
                                val loggedInStudent = Student(
                                    fullName = fullName,
                                    studentNumber = studentNumber,
                                    password = loginStudent.password, // Keep the entered password
                                    departmentID = departmentID,
                                    courseID = courseID
                                )

                                // Save student number in CartService
                                CartService.getInstance().saveStudentNumber(loggedInStudent.studentNumber)

                                // Initialize UserManager
                                UserManager.initialize(applicationContext)
                                val userManager = UserManager.getInstance()

                                // Store the complete user details
                                userManager.login(loggedInStudent)

                                Log.d("LoginSuccess", "Logged in student: $loggedInStudent")

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
                                Toast.makeText(this@Login, "Student data not found.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@Login, message.ifEmpty { "Incorrect Student Number or Password" }, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("LOGIN_ERROR", "Parse error: ${e.message}")
                        Toast.makeText(this@Login, "Error processing response. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("LOGIN_ERROR", "Error: ${response.code()}")
                    Toast.makeText(this@Login, "Login failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                submitButton.isEnabled = true
                Log.e("LOGIN_ERROR", "Failure: ${t.message}")
                Toast.makeText(this@Login, "Network error. Please check your connection.", Toast.LENGTH_LONG).show()
            }
        })
    }
}