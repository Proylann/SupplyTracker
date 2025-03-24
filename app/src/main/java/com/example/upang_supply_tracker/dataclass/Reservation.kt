package com.example.upang_supply_tracker.models

import com.google.gson.annotations.SerializedName

data class ReservationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("reservationId") val reservationId: Int? = null
)

data class ReservationRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("items") val items: List<CartItem>,
    @SerializedName("notes") val notes: String? = null
)