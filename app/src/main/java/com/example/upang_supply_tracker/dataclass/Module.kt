package com.example.upang_supply_tracker.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Module(
    @SerializedName("moduleId")
    val moduleId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("semester")
    val semester: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("departmentId")
    val departmentId: Int,
    @SerializedName("Name")
    val Name: String?,
    @SerializedName("courseId")
    val courseId: Int?,
    @SerializedName("CourseName")
    val courseName: String?,

    @SerializedName("imageData")
    val imageData: String? = null

) : Serializable

data class ModuleResponse(
    @SerializedName("modules")
    val modules: List<Module>?,

    @SerializedName("error")
    val error: String?
)