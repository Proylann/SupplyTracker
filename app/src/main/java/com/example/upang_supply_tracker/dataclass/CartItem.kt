package com.example.upang_supply_tracker.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CartItem(
    @SerializedName("uniformId")
    val uniformId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("departmentName")
    val departmentName: String,

    @SerializedName("courseName")
    val courseName: String?,

    @SerializedName("img")
    val img: String?,

    @SerializedName("quantity")
    var quantity: Int = 1
) : Serializable