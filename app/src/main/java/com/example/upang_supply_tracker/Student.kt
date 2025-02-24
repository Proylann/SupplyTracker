package com.example.upang_supply_tracker

import com.google.gson.annotations.SerializedName

data class Student(
    @SerializedName("FullName") val fullName: String,
    @SerializedName("StudentNumber") val studentNumber: String,
    @SerializedName("Password") val password: String,
    @SerializedName("Department") val department: String
)
