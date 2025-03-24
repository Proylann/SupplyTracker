package com.example.upang_supply_tracker.Services

import android.util.Log
import com.example.upang_supply_tracker.backend.ApiService
import com.example.upang_supply_tracker.backend.RetrofitClient
import com.example.upang_supply_tracker.models.CartItem
import com.example.upang_supply_tracker.models.ReservationRequest
import com.example.upang_supply_tracker.models.ReservationResponse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ReservationService {

    private var apiService = RetrofitClient.instance.create(ApiService::class.java)

    companion object {
        private var instance: ReservationService? = null

        fun getInstance(): ReservationService {
            if (instance == null) {
                instance = ReservationService()
            }
            return instance!!
        }
    }

    suspend fun submitReservation(
        studentNumber: String,
        cartItems: List<CartItem>,
        notes: String? = null
    ): Result<ReservationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val requestData = ReservationRequest(
                    userId = studentNumber,  // Pass the student number directly
                    items = cartItems,
                    notes = notes
                )

                val response = apiService.submitReservation(requestData)

                // Rest of the code remains the same
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(IOException("Empty response body"))
                } else {
                    Result.failure(IOException("Unexpected response ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e("ReservationService", "Error submitting reservation", e)
                Result.failure(e)
            }
        }
    }
}