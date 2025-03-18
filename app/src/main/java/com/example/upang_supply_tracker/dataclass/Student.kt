package com.example.upang_supply_tracker.dataclass

import com.google.gson.annotations.SerializedName

data class Student(
    @SerializedName("FullName") val fullName: String,
    @SerializedName("StudentNumber") val studentNumber: String,
    @SerializedName("Password") val password: String,
    @SerializedName("DepartmentID") val departmentID: String,
    @SerializedName("CourseID") val courseID: String
)
