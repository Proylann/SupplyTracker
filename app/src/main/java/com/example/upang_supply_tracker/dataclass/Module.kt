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

    @SerializedName("departmentName")
    val departmentName: String,

    @SerializedName("courseId")
    val courseId: Int?,

    @SerializedName("courseName")
    val courseName: String?
) : Serializable

data class ModuleResponse(
    @SerializedName("modules")
    val modules: List<Module>?,

    @SerializedName("error")
    val error: String?
)