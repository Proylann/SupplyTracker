package com.example.upang_supply_tracker.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.upang_supply_tracker.dataclass.Department
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.dataclass.Student
import com.example.upang_supply_tracker.backend.ApiService
import com.example.upang_supply_tracker.backend.RetrofitClient
import com.example.upang_supply_tracker.dataclass.Course
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Register : AppCompatActivity() {
    private lateinit var etFullname: EditText
    private lateinit var etStudentNumber: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var spDepartment: Spinner
    private lateinit var spCourse: Spinner
    private lateinit var btnSubmit: Button
    private lateinit var departmentList: MutableList<Department>
    private lateinit var departmentAdapter: ArrayAdapter<String>
    private lateinit var courseList: MutableList<Course>
    private lateinit var courseAdapter: ArrayAdapter<String>

    private lateinit var togglePassword: ImageButton
    private lateinit var togglePassword2: ImageButton
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private val apiService = RetrofitClient.instance.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI Components
        etFullname = findViewById(R.id.Fullname)
        etStudentNumber = findViewById(R.id.StudentNumber)
        etPassword = findViewById(R.id.Password)
        etConfirmPassword = findViewById(R.id.ConfirmPassword)
        spDepartment = findViewById(R.id.Department)
        spCourse = findViewById(R.id.Course)
        btnSubmit = findViewById(R.id.btnSubmit)
        togglePassword = findViewById(R.id.togglePassword)
        togglePassword2 = findViewById(R.id.togglePassword2)

        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(etPassword, isPasswordVisible, togglePassword)
        }

        togglePassword2.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(etConfirmPassword, isConfirmPasswordVisible, togglePassword2)
        }



        // Initialize department spinner
        departmentList = mutableListOf()
        departmentAdapter = ArrayAdapter(this, R.layout.spinner_item, mutableListOf("Loading..."))
        spDepartment.adapter = departmentAdapter

        // Initialize course spinner
        courseList = mutableListOf()
        courseAdapter = ArrayAdapter(this, R.layout.spinner_item, mutableListOf("Select Course"))
        spCourse.adapter = courseAdapter

        // Hide course spinner initially
        spCourse.visibility = View.GONE

        // Set listener for department selection
        spDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // If not "Select Department"
                    val selectedDepartment = departmentList[position - 1]
                    fetchCoursesByDepartment(selectedDepartment.DepartmentID)
                    spCourse.visibility = View.VISIBLE
                } else {
                    spCourse.visibility = View.GONE
                    courseAdapter.clear()
                    courseAdapter.add("Select Course")
                    courseAdapter.notifyDataSetChanged()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                spCourse.visibility = View.GONE
            }
        }

        fetchDepartments() // Fetch departments from API

        btnSubmit.setOnClickListener {
            submitData()
        }
    }

    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean, button: ImageButton) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            button.setImageResource(R.drawable.ice_eye)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            button.setImageResource(R.drawable.ice_eye)
        }
        editText.setSelection(editText.text.length)
    }




    private fun fetchDepartments() {
        apiService.getDepartments().enqueue(object : Callback<List<Department>> {
            override fun onResponse(call: Call<List<Department>>, response: Response<List<Department>>) {
                if (response.isSuccessful) {
                    departmentList.clear()
                    departmentList.addAll(response.body() ?: emptyList())

                    val departmentNames = mutableListOf("Select Department")
                    departmentList.forEach { departmentNames.add(it.Name) }

                    departmentAdapter.clear()
                    departmentAdapter.addAll(departmentNames)
                    departmentAdapter.notifyDataSetChanged()
                } else {
                    Log.e("API_ERROR", "Failed to load departments")
                    Toast.makeText(this@Register, "Failed to load departments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Log.e("API_ERROR", "Network Error: ${t.message}")
                Toast.makeText(this@Register, "Network error fetching departments", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCoursesByDepartment(departmentId: String) {
        courseAdapter.clear()
        courseAdapter.add("Loading courses...")
        courseAdapter.notifyDataSetChanged()

        apiService.getCourse(departmentId).enqueue(object : Callback<List<Course>> {
            override fun onResponse(call: Call<List<Course>>, response: Response<List<Course>>) {
                if (response.isSuccessful) {
                    courseList.clear()
                    courseList.addAll(response.body() ?: emptyList())

                    val courseNames = mutableListOf("Select Course")
                    courseList.forEach { courseNames.add(it.CourseName) }

                    courseAdapter.clear()
                    courseAdapter.addAll(courseNames)
                    courseAdapter.notifyDataSetChanged()
                } else {
                    Log.e("API_ERROR", "Failed to load courses. Code: ${response.code()}")
                    courseAdapter.clear()
                    courseAdapter.add("Select Course")
                    courseAdapter.add("Failed to load courses")
                    courseAdapter.notifyDataSetChanged()
                    Toast.makeText(this@Register, "Failed to load courses", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Course>>, t: Throwable) {
                Log.e("API_ERROR", "Network Error: ${t.message}")
                courseAdapter.clear()
                courseAdapter.add("Select Course")
                courseAdapter.add("Network error")
                courseAdapter.notifyDataSetChanged()
                Toast.makeText(this@Register, "Network error fetching courses", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun submitData() {
        val fullname = etFullname.text.toString().trim()
        val studentNumber = etStudentNumber.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val departmentPosition = spDepartment.selectedItemPosition
        val coursePosition = spCourse.selectedItemPosition

        // Validate form
        if (fullname.isEmpty() || studentNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        if (departmentPosition == 0) {
            Toast.makeText(this, "Please select a valid department", Toast.LENGTH_SHORT).show()
            return
        }

        if (coursePosition == 0) {
            Toast.makeText(this, "Please select a valid course", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDepartment = departmentList[departmentPosition - 1] // Adjust for "Select Department"
        val selectedCourse = courseList[coursePosition - 1] // Adjust for "Select Course"

        val student = Student(
            fullName = fullname,
            studentNumber = studentNumber,
            password = password,
            departmentID = selectedDepartment.DepartmentID,
            courseID = selectedCourse.CourseID
        )
        val json = Gson().toJson(student)
        Log.d("API_REQUEST", "Sending JSON: $json")

        apiService.createStudent(student).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@Register, "Student added successfully!", Toast.LENGTH_SHORT).show()
                    // Reset form fields
                    etFullname.text.clear()
                    etStudentNumber.text.clear()
                    etPassword.text.clear()
                    spDepartment.setSelection(0)
                    spCourse.visibility = View.GONE

                    val intent = Intent(this@Register, Login::class.java)
                    startActivity(intent)

                } else {
                    Log.e("API_ERROR", "Error Code: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@Register, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("API_ERROR", "Network Failure: ${t.message}")
                Toast.makeText(this@Register, "Failed to connect to server", Toast.LENGTH_SHORT).show()
            }
        })
    }
}