package com.example.upang_supply_tracker.Services

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.upang_supply_tracker.dataclass.Books
import com.example.upang_supply_tracker.models.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CartService private constructor(context: Context) {
    private val cartItems = mutableListOf<CartItem>()
    private val sharedPreferences: SharedPreferences
    private val gson = Gson()

    // LiveData for observers
    private val _cartItemsLiveData = MutableLiveData<List<CartItem>>(emptyList())
    val cartItemsLiveData: LiveData<List<CartItem>> = _cartItemsLiveData

    init {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadCart()
        updateLiveData()
    }

    companion object {
        private const val PREF_NAME = "CartPreferences"
        private const val KEY_CART_ITEMS = "cart_items"
        private const val KEY_STUDENT_NUMBER = "student_number"

        @Volatile
        private var instance: CartService? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = CartService(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): CartService {
            return instance ?: throw IllegalStateException("CartService must be initialized first")
        }
    }

    fun addToCart(item: CartItem) {
        // Check if the item is already in the cart
        val existingItem = cartItems.find { it.itemId == item.itemId && it.itemType == item.itemType }

        if (existingItem != null) {
            // If the item is already in the cart, increment its quantity
            existingItem.quantity += item.quantity
        } else {
            // Otherwise, add the new item
            cartItems.add(item)
        }

        // Save the updated cart to SharedPreferences
        saveCart()
        updateLiveData()
    }

    // Re-implemented function to add book to cart
    fun addBookToCart(book: Books) {
        val cartItem = CartItem(
            itemId = book.ID.toInt(),
            name = book.BookTitle,
            description = "${book.Department} textbook",
            departmentName = book.Department,
            courseName = book.Course,
            img = book.Preview,
            quantity = 1,
            itemType = "BOOK"
        )

        addToCart(cartItem)
    }

    fun removeFromCart(itemId: Int) {
        cartItems.removeIf { it.itemId == itemId }
        saveCart()
        updateLiveData()
    }

    // Original overloaded method for backward compatibility
    fun removeFromCart(item: CartItem) {
        cartItems.removeIf { it.itemId == item.itemId && it.itemType == item.itemType }
        saveCart()
        updateLiveData()
    }

    // Original method signature for backward compatibility
    fun updateQuantity(itemId: Int, quantity: Int) {
        val existingItem = cartItems.find { it.itemId == itemId }
        if (existingItem != null) {
            existingItem.quantity = quantity
            saveCart()
            updateLiveData()
        }
    }

    // New method signature
    fun updateQuantity(item: CartItem, quantity: Int) {
        val existingItem = cartItems.find { it.itemId == item.itemId && it.itemType == item.itemType }
        existingItem?.quantity = quantity
        saveCart()
        updateLiveData()
    }

    fun getCartItems(): List<CartItem> {
        return cartItems.toList()
    }



    fun clearCart() {
        cartItems.clear()
        saveCart()
        updateLiveData()
    }

    fun saveStudentNumber(studentNumber: String) {
        // Save the student number to associate cart with specific user
        sharedPreferences.edit().putString(KEY_STUDENT_NUMBER, studentNumber).apply()
    }

    fun getStudentNumber(): String? {
        return sharedPreferences.getString(KEY_STUDENT_NUMBER, null)
    }

    private fun saveCart() {
        // Convert cart items to JSON string
        val jsonCartItems = gson.toJson(cartItems)

        // Save to SharedPreferences
        sharedPreferences.edit().putString(KEY_CART_ITEMS, jsonCartItems).apply()
    }

    private fun loadCart() {
        // Get JSON string from SharedPreferences
        val jsonCartItems = sharedPreferences.getString(KEY_CART_ITEMS, null)

        if (!jsonCartItems.isNullOrEmpty()) {
            // Convert JSON string back to list of CartItems
            val type = object : TypeToken<List<CartItem>>() {}.type
            val items: List<CartItem> = gson.fromJson(jsonCartItems, type)

            // Clear current cart and add loaded items
            cartItems.clear()
            cartItems.addAll(items)
            updateLiveData()
        }
    }

    // Helper method to update LiveData with current cart items
    private fun updateLiveData() {
        _cartItemsLiveData.postValue(cartItems.toList())
    }
}