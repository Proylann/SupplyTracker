package com.example.upang_supply_tracker.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.upang_supply_tracker.Services.CartService
import com.example.upang_supply_tracker.models.CartItem

class CartViewModel : ViewModel() {
    private val cartService = CartService.getInstance()

    // Expose the cart items LiveData from the service
    val cartItems: LiveData<List<CartItem>> = cartService.cartItemsLiveData

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
        clearCart()
    }
}