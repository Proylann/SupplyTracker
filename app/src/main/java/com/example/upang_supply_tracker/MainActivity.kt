package com.example.upang_supply_tracker

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var etFullname: EditText
    private lateinit var etStudentNumber: EditText
    private lateinit var etPassword: EditText
    private lateinit var spDepartment: Spinner
    private lateinit var btnSubmit: Button
    private val apiService = RetrofitClient.instance.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI Components
        etFullname = findViewById(R.id.Fullname)
        etStudentNumber = findViewById(R.id.StudentNumber)
        etPassword = findViewById(R.id.Password)
        spDepartment = findViewById(R.id.Department)
        btnSubmit = findViewById(R.id.btnSubmit)

        // Department List for Spinner
        val departments = listOf("Select Department", "CITE", "CAHS", "CAS", "CEA", "CELA", "CCJE")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, departments)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spDepartment.adapter = adapter


        btnSubmit.setOnClickListener {
            submitData()
        }
    }


    private fun submitData() {
        val fullname = etFullname.text.toString().trim()
        val studentNumber = etStudentNumber.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val department = spDepartment.selectedItem?.toString()

        if (department.isNullOrEmpty() || department == "Select Department") {
            Toast.makeText(this, "Please select a valid department", Toast.LENGTH_SHORT).show()
            return
        }
        if (fullname.isEmpty() || studentNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }


        val student = Student(fullname, studentNumber, password, department)


        val json = Gson().toJson(student)
        Log.d("API_REQUEST", "Sending JSON: $json")

        apiService.createStudent(student).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Student added successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("API_ERROR", "Error Code: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MainActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("API_ERROR", "Network Failure: ${t.message}")
                Toast.makeText(this@MainActivity, "Failed to connect to server", Toast.LENGTH_SHORT).show()
            }
        })
    }
}