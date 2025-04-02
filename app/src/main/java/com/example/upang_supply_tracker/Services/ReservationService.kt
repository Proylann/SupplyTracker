package com.example.upang_supply_tracker.Services

import android.util.Log
import com.example.upang_supply_tracker.backend.ApiService
import com.example.upang_supply_tracker.backend.RetrofitClient
import com.example.upang_supply_tracker.dataclass.CartItem
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
    private lateinit var cartService: CartService
    private lateinit var reservationValidator: ReservationValidator

    // Track reservations already processed to avoid duplicate markings
    private val processedReservations = mutableSetOf<Int>()

    companion object {
        private var instance: ReservationService? = null
        private const val TAG = "ReservationService"

        fun getInstance(): ReservationService {
            if (instance == null) {
                instance = ReservationService()
            }
            return instance!!
        }
    }

    init {
        try {
            cartService = CartService.getInstance()
            reservationValidator = ReservationValidator.getInstance()
        } catch (e: IllegalStateException) {
            // Services may not be initialized yet
            Log.e(TAG, "Services not initialized: ${e.message}")
        }
    }

    suspend fun submitReservation(
        studentNumber: String,
        cartItems: List<CartItem>,
        notes: String? = null
    ): Result<ReservationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // First check if all items can be reserved
                val alreadyReservedItems = reservationValidator.getAlreadyReservedItems(studentNumber, cartItems)

                if (alreadyReservedItems.isNotEmpty()) {
                    val itemNames = alreadyReservedItems.joinToString(", ") { it.name }
                    return@withContext Result.failure(IOException("Cannot reserve. The following items have already been reserved: $itemNames"))
                }

                val requestData = ReservationRequest(
                    userId = studentNumber,
                    items = cartItems,
                    notes = notes
                )

                val response = apiService.submitReservation(requestData)

                if (response.isSuccessful) {
                    response.body()?.let {
                        // Mark items as reserved after successful submission
                        reservationValidator.markItemsAsReserved(studentNumber, cartItems)
                        Result.success(it)
                    } ?: Result.failure(IOException("Empty response body"))
                } else {
                    Result.failure(IOException("Unexpected response ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting reservation", e)
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
                                val reservationId = reservationObj.optInt("id")

                                // Process books
                                val booksArray = reservationObj.optJSONArray("books") ?: JSONArray()
                                val itemsList = mutableListOf<CartItem>()

                                for (j in 0 until booksArray.length()) {
                                    val bookObj = booksArray.getJSONObject(j)
                                    val imageData = bookObj.optString("preview", null)

                                    Log.d(TAG, "Book image data present: ${!imageData.isNullOrEmpty()}")

                                    val cartItem = CartItem(
                                        itemId = bookObj.optInt("id"),
                                        name = bookObj.optString("title", ""),
                                        departmentName = bookObj.optString("departmentId", ""),
                                        courseName = bookObj.optString("courseId", ""),
                                        img = imageData, // This will be a data URL with base64 encoding
                                        quantity = bookObj.optInt("quantity", 1),
                                        itemType = "BOOK",
                                        size = null
                                    )
                                    itemsList.add(cartItem)
                                    Log.d(TAG, "Added book: ${cartItem.name}, has image: ${!cartItem.img.isNullOrEmpty()}")
                                }

                                // Process modules
                                val modulesArray = reservationObj.optJSONArray("modules") ?: JSONArray()
                                for (j in 0 until modulesArray.length()) {
                                    val moduleObj = modulesArray.getJSONObject(j)
                                    val imageData = moduleObj.optString("preview", null)

                                    Log.d(TAG, "Module image data present: ${!imageData.isNullOrEmpty()}")

                                    val cartItem = CartItem(
                                        itemId = moduleObj.optInt("id"),
                                        name = moduleObj.optString("title", ""),
                                        departmentName = moduleObj.optString("departmentId", ""),
                                        courseName = moduleObj.optString("courseId", ""),
                                        img = imageData, // This will be a data URL with base64 encoding
                                        quantity = moduleObj.optInt("quantity", 1),
                                        itemType = "MODULE",
                                        size = null
                                    )
                                    itemsList.add(cartItem)
                                    Log.d(TAG, "Added module: ${cartItem.name}, has image: ${!cartItem.img.isNullOrEmpty()}")
                                }

                                // Process uniforms
                                val uniformsArray = reservationObj.optJSONArray("uniforms") ?: JSONArray()
                                for (j in 0 until uniformsArray.length()) {
                                    val uniformObj = uniformsArray.getJSONObject(j)
                                    val imageData = uniformObj.optString("img", null)

                                    Log.d(TAG, "Uniform image data present: ${!imageData.isNullOrEmpty()}")

                                    val cartItem = CartItem(
                                        itemId = uniformObj.optInt("id"),
                                        name = uniformObj.optString("name", ""),
                                        departmentName = uniformObj.optString("departmentId", ""),
                                        courseName = uniformObj.optString("courseId", ""),
                                        img = imageData, // This will be a data URL with base64 encoding
                                        quantity = uniformObj.optInt("quantity", 1),
                                        itemType = "UNIFORM",
                                        size = uniformObj.optString("size", "")
                                    )
                                    itemsList.add(cartItem)
                                    Log.d(TAG, "Added uniform: ${cartItem.name}, size: ${cartItem.size}, has image: ${!cartItem.img.isNullOrEmpty()}")
                                }
                                val reservation = Reservation(
                                    id = reservationId,
                                    date = reservationObj.optString("date"),
                                    status = reservationObj.optString("status"),
                                    items = itemsList
                                )

                                reservationsList.add(reservation)

                                // Only mark items as reserved if we haven't processed this reservation before
                                if (::reservationValidator.isInitialized && !processedReservations.contains(reservationId)) {
                                    reservationValidator.markItemsAsReserved(studentNumber, itemsList)
                                    processedReservations.add(reservationId)
                                    Log.d(TAG, "Marked reservation #$reservationId as processed")
                                }
                            }

                            onSuccess(reservationsList)
                        } else {
                            val message =
                                jsonResponse.optString("message", "Failed to fetch reservations")
                            onError(message)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing response", e)
                        onError("Error parsing response: ${e.message}")
                    }
                } else {
                    onError("Server error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "Network failure", t)
                onError("Network error: ${t.message}")
            }
        })
    }

    fun updateReservationStatus(
        studentNumber: String,
        reservationId: Int,
        newStatus: String,
        items: List<CartItem>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (newStatus.equals("CANCELLED", ignoreCase = true) && ::reservationValidator.isInitialized) {
            reservationValidator.removeReservedItems(studentNumber, items)
            // Remove from processed set when cancelled
            processedReservations.remove(reservationId)
            onSuccess()
        } else {
            // Implement your API call here
            onSuccess() // Placeholder
        }
    }

    fun clearProcessedReservations() {
        processedReservations.clear()
    }
}