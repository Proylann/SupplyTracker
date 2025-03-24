package com.example.upang_supply_tracker.ui.cart

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upang_supply_tracker.Services.CartService
import com.example.upang_supply_tracker.Services.UserManager
import com.example.upang_supply_tracker.models.CartItem
import com.example.upang_supply_tracker.Services.ReservationService
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    private val cartService = CartService.getInstance()
    private val reservationService = ReservationService.getInstance()
    private val userManager = UserManager.getInstance()

    // Expose the cart items LiveData from the service
    val cartItems: LiveData<List<CartItem>> = cartService.cartItemsLiveData

    // Status message for UI feedback
    private val _reservationStatus = MutableLiveData<String>()
    val reservationStatus: LiveData<String> = _reservationStatus

    // Loading state for UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun updateQuantity(uniformId: Int, quantity: Int) {
        cartService.updateQuantity(uniformId, quantity)
    }

    fun removeFromCart(uniformId: Int) {
        cartService.removeFromCart(uniformId)
    }

    fun clearCart() {
        cartService.clearCart()
    }

    fun checkoutCart() {
        val items = cartItems.value ?: return
        if (items.isEmpty()) {
            _reservationStatus.value = "Cart is empty"
            return
        }

        val currentStudent = userManager.getCurrentUser()
        if (currentStudent == null) {
            _reservationStatus.value = "User not logged in"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            val result = reservationService.submitReservation(currentStudent.studentNumber, items)

            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.success == true) {
                    _reservationStatus.postValue("Reservation submitted successfully")
                    clearCart()
                } else {
                    _reservationStatus.postValue("Error: ${response?.message ?: "Unknown error"}")
                }
            } else {
                _reservationStatus.postValue("Network error: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
                Log.e("CheckoutError", result.exceptionOrNull()?.message ?: "Unknown error")
            }

            _isLoading.postValue(false)
        }
    }
}