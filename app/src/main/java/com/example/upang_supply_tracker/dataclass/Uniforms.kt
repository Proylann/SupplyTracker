package com.example.upang_supply_tracker.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Uniform(
    @SerializedName("uniformId")
    val uniformId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("size")
    val size: String? = "",  // Added size field

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("departmentId")
    val departmentId: Int,

    @SerializedName("departmentName")
    val departmentName: String,

    @SerializedName("courseId")
    val courseId: Int?,

    @SerializedName("courseName")
    val courseName: String?,

    @SerializedName("img")
    val img: String?
) : Serializable


data class UniformResponse(
    @SerializedName("uniforms")
    val uniforms: List<Uniform>?,

    @SerializedName("error")
    val error: String?
)