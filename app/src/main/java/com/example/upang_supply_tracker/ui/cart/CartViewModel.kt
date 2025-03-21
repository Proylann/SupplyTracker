package com.example.upang_supply_tracker.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.upang_supply_tracker.models.CartItem

class CartViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    init {
        _cartItems.value = emptyList()
    }

    fun setCartItems(items: List<CartItem>) {
        _cartItems.value = items
    }

    fun updateQuantity(uniformId: Int, quantity: Int) {
        val currentItems = _cartItems.value?.toMutableList() ?: mutableListOf()
        val itemIndex = currentItems.indexOfFirst { it.uniformId == uniformId }

        if (itemIndex != -1) {
            currentItems[itemIndex] = currentItems[itemIndex].copy(quantity = quantity)
            _cartItems.value = currentItems
        }
    }

    fun removeFromCart(uniformId: Int) {
        val currentItems = _cartItems.value?.toMutableList() ?: mutableListOf()
        val updatedItems = currentItems.filter { it.uniformId != uniformId }
        _cartItems.value = updatedItems
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun checkoutCart() {
        // Here you would implement the actual checkout logic
        // such as sending the cart items to an API

        // For now, just clear the cart after checkout
        clearCart()
    }
}