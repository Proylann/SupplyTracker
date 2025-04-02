package com.example.upang_supply_tracker.Services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.upang_supply_tracker.dataclass.Student

class UserManager private constructor(context: Context) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val TAG = "UserManager"

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
        // Check if this is a different user from the previously logged in user
        val previousUserId = getCurrentUserId()
        val newUserId = student.studentNumber

        // Update current user
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

        // Clear reservation data if user changed
        if (previousUserId != newUserId) {
            try {
                // Clear any previous reservation data for this user
                ReservationValidator.getInstance().clearUserReservations(student.studentNumber)

                // Also clear processed reservations in the reservation service
                ReservationService.getInstance().clearProcessedReservations()

                // Reload actual reservations from server
                ReservationService.getInstance().getReservations(
                    student.studentNumber,
                    onSuccess = { /* Reservations loaded automatically */ },
                    onError = { errorMsg ->
                        Log.e(TAG, "Failed to load user reservations: $errorMsg")
                    }
                )

                Log.d(TAG, "Cleared previous reservation data for new user login: $newUserId")
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Services not initialized: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing reservation data: ${e.message}")
            }
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
        try {
            // Clear reservation data for current user
            currentUser?.studentNumber?.let { studentId ->
                ReservationValidator.getInstance().clearUserReservations(studentId)
            }

            // Clear processed reservations
            ReservationService.getInstance().clearProcessedReservations()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Services not initialized: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing reservation data: ${e.message}")
        }

        // Clear user data
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