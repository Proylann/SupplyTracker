package com.example.upang_supply_tracker.Services;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.upang_supply_tracker.dataclass.Books;
import com.example.upang_supply_tracker.dataclass.CartItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

class CartService private constructor(context: Context) {
    private val cartItems = mutableListOf<CartItem>()
    private val sharedPreferences: SharedPreferences
    private val gson = Gson()

    // Reference to the reservation validator
    private val reservationValidator = ReservationValidator.getInstance()

    // LiveData for observers
    private val _cartItemsLiveData = MutableLiveData<List<CartItem>>(emptyList())
    val cartItemsLiveData: LiveData<List<CartItem>> = _cartItemsLiveData

    // Add a dedicated LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

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
                        // Make sure ReservationValidator is initialized
                        ReservationValidator.initialize(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): CartService {
            return instance ?: throw IllegalStateException("CartService must be initialized first")
        }
    }

    /**
     * Add item to cart only if it hasn't been reserved already and isn't already in the cart
     * @return true if item was added, false if it wasn't added
     */
    fun addToCart(item: CartItem): Boolean {
        val studentNumber = getStudentNumber() ?: return false

        // Check if this item is already reserved by the user
        if (!reservationValidator.canReserveItem(studentNumber, item)) {
            _errorMessage.postValue("This item has already been reserved by you.")
            return false
        }

        // Check if the item is already in the cart (1 to 1 relationship)
        val existingItem = cartItems.find { it.itemId == item.itemId && it.itemType == item.itemType }

        if (existingItem != null) {
            // Item is already in cart, don't add again and notify user
            _errorMessage.postValue("This item is already in your cart.")
            return false
        } else {
            // Add the new item with quantity always set to 1
            item.quantity = 1
            cartItems.add(item)
        }

        // Save the updated cart to SharedPreferences
        saveCart()
        updateLiveData()
        return true
    }

    // Re-implemented function to add book to cart
    fun addBookToCart(book: Books): Boolean {
        val cartItem = CartItem(
            itemId = book.ID.toInt(),
            name = book.BookTitle,
            departmentName = book.Department,
            courseName = book.Course,
            img = book.Preview,
            quantity = 1,
            itemType = "BOOK",
            size = null  // Books don't have sizes
        )

        return addToCart(cartItem)
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

    /**
     * Check if an item is already in the cart
     * @return true if the item is in the cart, false otherwise
     */
    fun isItemInCart(itemId: Int, itemType: String): Boolean {
        return cartItems.any { it.itemId == itemId && it.itemType == itemType }
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

    // Add this new method for updating sizes
    fun updateSize(itemId: Int, size: String) {
        val existingItem = cartItems.find { it.itemId == itemId }
        if (existingItem != null) {
            existingItem.size = size
            saveCart()
            updateLiveData()
        }
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

    /**
     * Check if any items in the cart have already been reserved
     * @return true if all items can be reserved, false otherwise
     */
    fun validateCartForCheckout(): Boolean {
        val studentNumber = getStudentNumber() ?: return false
        val alreadyReservedItems = reservationValidator.getAlreadyReservedItems(studentNumber, cartItems)

        if (alreadyReservedItems.isNotEmpty()) {
            val itemNames = alreadyReservedItems.joinToString(", ") { it.name }
            _errorMessage.postValue("Cannot checkout. The following items have already been reserved: $itemNames")
            return false
        }

        return true
    }

    /**
     * Mark all items in cart as reserved after successful reservation
     */
    fun markCartItemsAsReserved() {
        val studentNumber = getStudentNumber() ?: return
        reservationValidator.markItemsAsReserved(studentNumber, cartItems)
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