package com.example.upang_supply_tracker.ui.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.upang_supply_tracker.Services.CartService
import com.example.upang_supply_tracker.Services.ReservationService
import com.example.upang_supply_tracker.Services.UserManager
import com.example.upang_supply_tracker.dataclass.CartItem
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val cartService = CartService.getInstance()
    private val reservationService = ReservationService.getInstance()
    private val userManager = UserManager.getInstance()

    // LiveData
    val cartItems: LiveData<List<CartItem>> = cartService.cartItemsLiveData
    val errorMessage: LiveData<String> = cartService.errorMessage

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _reservationStatus = MutableLiveData<String>()
    val reservationStatus: LiveData<String> = _reservationStatus

    fun updateQuantity(itemId: Int, quantity: Int) {
        cartService.updateQuantity(itemId, quantity)
    }

    fun updateSize(itemId: Int, size: String) {
        cartService.updateSize(itemId, size)
    }

    fun removeFromCart(itemId: Int) {
        cartService.removeFromCart(itemId)
    }

    fun clearCart() {
        cartService.clearCart()
    }

    fun checkoutCart() {
        val items = cartService.getCartItems()
        if (items.isEmpty()) {
            _reservationStatus.value = "Your cart is empty"
            return
        }

        val studentNumber = userManager.getCurrentUserId()
        if (studentNumber.isEmpty()) {
            _reservationStatus.value = "Please log in to continue"
            return
        }

        // First validate that all items can be reserved
        if (!cartService.validateCartForCheckout()) {
            // Error message is set by CartService
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = reservationService.submitReservation(studentNumber, items)
            _isLoading.value = false

            result.fold(
                onSuccess = {
                    _reservationStatus.value = "Reservation submitted successfully"
                    // After successful reservation, mark items in cart as reserved
                    cartService.markCartItemsAsReserved()
                    cartService.clearCart()
                },
                onFailure = {
                    _reservationStatus.value = "Failed to submit reservation: ${it.message}"
                }
            )
        }
    }
}