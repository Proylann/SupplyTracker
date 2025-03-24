package com.example.upang_supply_tracker.Services

import android.util.Log
import com.example.upang_supply_tracker.backend.ApiService
import com.example.upang_supply_tracker.backend.RetrofitClient
import com.example.upang_supply_tracker.models.CartItem
import com.example.upang_supply_tracker.models.Reservation
import com.example.upang_supply_tracker.models.ReservationRequest
import com.example.upang_supply_tracker.models.ReservationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
                    userId = studentNumber,
                    items = cartItems,
                    notes = notes
                )

                val response = apiService.submitReservation(requestData)

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

    // Fixed method to get reservations
    fun getReservations(
        studentNumber: String,
        onSuccess: (List<Reservation>) -> Unit,
        onError: (String) -> Unit
    ) {
        val call = apiService.getReservations(studentNumber)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseString = response.body()?.string() ?: ""
                        val jsonResponse = JSONObject(responseString)

                        val success = jsonResponse.optBoolean("success", false)

                        if (success) {
                            val reservationsArray = jsonResponse.getJSONArray("data")
                            val reservationsList = mutableListOf<Reservation>()

                            for (i in 0 until reservationsArray.length()) {
                                val reservationObj = reservationsArray.getJSONObject(i)

                                // Process books
                                val booksArray = reservationObj.optJSONArray("books") ?: JSONArray()
                                val itemsList = mutableListOf<CartItem>()

                                for (j in 0 until booksArray.length()) {
                                    val bookObj = booksArray.getJSONObject(j)
                                    val bookImageData = bookObj.optString("preview", null)
                                    Log.d("ReservationService", "Book image data: $bookImageData")
                                    val cartItem = CartItem(
                                        itemId = bookObj.optInt("id"),
                                        name = bookObj.optString("title", ""),
                                        description = "",
                                        departmentName = bookObj.optString("departmentId", ""),
                                        courseName = bookObj.optString("courseId", ""),
                                        img = bookObj.optString("preview", null), // Update to "preview" if that's what your API returns
                                        quantity = bookObj.optInt("quantity", 1),
                                        itemType = "BOOK"
                                    )
                                    itemsList.add(cartItem)
                                }

                                // Process modules
                                val modulesArray =
                                    reservationObj.optJSONArray("modules") ?: JSONArray()
                                for (j in 0 until modulesArray.length()) {
                                    val moduleObj = modulesArray.getJSONObject(j)
                                    val cartItem = CartItem(
                                        itemId = moduleObj.optInt("id"),
                                        name = moduleObj.optString("title", ""),
                                        description = "",
                                        departmentName = moduleObj.optString("departmentId", ""),
                                        courseName = moduleObj.optString("courseId", ""),
                                        img = moduleObj.optString("preview", null), // Update field name if different
                                        quantity = moduleObj.optInt("quantity", 1),
                                        itemType = "MODULE"
                                    )
                                    itemsList.add(cartItem)
                                }

                                // Process uniforms
                                val uniformsArray =
                                    reservationObj.optJSONArray("uniforms") ?: JSONArray()
                                for (j in 0 until uniformsArray.length()) {
                                    val uniformObj = uniformsArray.getJSONObject(j)
                                    val cartItem = CartItem(
                                        itemId = uniformObj.optInt("id"),
                                        name = uniformObj.optString("name", ""),
                                        description = uniformObj.optString("description", ""),
                                        departmentName = uniformObj.optString("departmentId", ""),
                                        courseName = uniformObj.optString("courseId", ""),
                                        img = uniformObj.optString("img", null), // Your Uniform model has "img" field
                                        quantity = uniformObj.optInt("quantity", 1),
                                        itemType = "UNIFORM"
                                    )
                                    itemsList.add(cartItem)
                                }

                                val reservation = Reservation(
                                    id = reservationObj.optInt("id"),
                                    date = reservationObj.optString("date"),
                                    status = reservationObj.optString("status"),
                                    items = itemsList
                                )

                                reservationsList.add(reservation)
                            }

                            onSuccess(reservationsList)
                        } else {
                            val message =
                                jsonResponse.optString("message", "Failed to fetch reservations")
                            onError(message)
                        }
                    } catch (e: Exception) {
                        onError("Error parsing response: ${e.message}")
                    }
                } else {
                    onError("Server error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        })
    }
}