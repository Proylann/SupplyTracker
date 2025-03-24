package com.example.upang_supply_tracker.Services

import android.content.Context
import android.content.SharedPreferences
import com.example.upang_supply_tracker.dataclass.Student

class UserManager private constructor(context: Context) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private var instance: UserManager? = null

        fun initialize(context: Context) {
            if (instance == null) {
                instance = UserManager(context.applicationContext)
            }
        }

        fun getInstance(): UserManager {
            return instance ?: throw IllegalStateException("UserManager must be initialized first")
        }
    }

    private var currentUser: Student? = null

    fun login(student: Student) {
        currentUser = student

        // Save to SharedPreferences
        with(sharedPrefs.edit()) {
            putString("student_number", student.studentNumber)
            putString("full_name", student.fullName)
            putString("department_id", student.departmentID)
            putString("course_id", student.courseID)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun loadUserFromPrefs() {
        if (sharedPrefs.getBoolean("is_logged_in", false)) {
            val studentNumber = sharedPrefs.getString("student_number", "")
            if (!studentNumber.isNullOrEmpty()) {
                currentUser = Student(
                    fullName = sharedPrefs.getString("full_name", "") ?: "",
                    studentNumber = studentNumber,
                    password = "", // Don't store or load password from prefs
                    departmentID = sharedPrefs.getString("department_id", "") ?: "",
                    courseID = sharedPrefs.getString("course_id", "") ?: ""
                )
            }
        }
    }

    fun logout() {
        currentUser = null
        with(sharedPrefs.edit()) {
            clear()
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return currentUser != null || sharedPrefs.getBoolean("is_logged_in", false)
    }

    fun getCurrentUser(): Student? {
        if (currentUser == null) {
            loadUserFromPrefs()
        }
        return currentUser
    }

    fun getCurrentUserId(): String {
        return getCurrentUser()?.studentNumber ?: ""
    }
}