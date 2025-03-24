package com.example.upang_supply_tracker.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Keep your existing models
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

// Add the Reservation model needed for the adapter
data class Reservation(
    @SerializedName("id") val id: Int,
    @SerializedName("date") val date: String,
    @SerializedName("status") val status: String,
    @SerializedName("items") val items: List<CartItem> = emptyList()
) : Serializable {

    fun getItemsSummary(): String {
        val itemTypeCounts = mutableMapOf<String, Int>()

        // Count items by type
        items.forEach { item ->
            val type = when (item.itemType) {
                "BOOK" -> "Book"
                "UNIFORM" -> "Uniform"
                "MODULE" -> "Module"
                else -> item.itemType
            }
            itemTypeCounts[type] = (itemTypeCounts[type] ?: 0) + item.quantity
        }

        // Build summary string
        return itemTypeCounts.entries.joinToString(", ") { (type, count) ->
            "$count ${if (count > 1) "${type}s" else type}"
        }.ifEmpty { "No items" }
    }
}