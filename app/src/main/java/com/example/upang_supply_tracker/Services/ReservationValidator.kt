package com.example.upang_supply_tracker.Services

import android.content.Context
import android.content.SharedPreferences
import com.example.upang_supply_tracker.dataclass.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Service to validate and track previously reserved items to prevent duplicate reservations
 */
class ReservationValidator private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val gson = Gson()

    // Map to store reserved items by user ID
    // Key: Student Number, Value: Set of reserved item IDs with type
    private val reservedItemsMap = mutableMapOf<String, MutableSet<String>>()

    init {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadReservedItems()
    }

    companion object {
        private const val PREF_NAME = "ReservationPreferences"
        private const val KEY_RESERVED_ITEMS = "reserved_items_map"

        @Volatile
        private var instance: ReservationValidator? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = ReservationValidator(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): ReservationValidator {
            return instance ?: throw IllegalStateException("ReservationValidator must be initialized first")
        }
    }

    /**
     * Checks if the given item is already reserved by the user
     * @return true if the item can be added to cart, false if already reserved
     */
    fun canReserveItem(studentId: String, item: CartItem): Boolean {
        val itemKey = "${item.itemId}:${item.itemType}"
        val userReservedItems = reservedItemsMap[studentId] ?: return true
        return !userReservedItems.contains(itemKey)
    }

    /**
     * Checks a list of items to see if any are already reserved
     * @return List of items that are already reserved
     */
    fun getAlreadyReservedItems(studentId: String, items: List<CartItem>): List<CartItem> {
        val userReservedItems = reservedItemsMap[studentId] ?: return emptyList()
        return items.filter {
            val itemKey = "${it.itemId}:${it.itemType}"
            userReservedItems.contains(itemKey)
        }
    }

    /**
     * Mark items as reserved after successful reservation
     */
    fun markItemsAsReserved(studentId: String, items: List<CartItem>) {
        val userReservedItems = reservedItemsMap.getOrPut(studentId) { mutableSetOf() }

        items.forEach { item ->
            val itemKey = "${item.itemId}:${item.itemType}"
            userReservedItems.add(itemKey)
        }

        saveReservedItems()
    }

    /**
     * Load reserved items from SharedPreferences
     */
    private fun loadReservedItems() {
        val jsonReservedItems = sharedPreferences.getString(KEY_RESERVED_ITEMS, null)

        if (!jsonReservedItems.isNullOrEmpty()) {
            val type = object : TypeToken<Map<String, MutableSet<String>>>() {}.type
            val loadedMap: Map<String, MutableSet<String>> = gson.fromJson(jsonReservedItems, type)
            reservedItemsMap.clear()
            reservedItemsMap.putAll(loadedMap)
        }
    }

    /**
     * Save reserved items to SharedPreferences
     */
    private fun saveReservedItems() {
        val jsonReservedItems = gson.toJson(reservedItemsMap)
        sharedPreferences.edit().putString(KEY_RESERVED_ITEMS, jsonReservedItems).apply()
    }

    fun clearAllReservationRecords() {
        reservedItemsMap.clear()
        sharedPreferences.edit().remove(KEY_RESERVED_ITEMS).apply()
    }

    /**
     * Remove specific items from a user's reservation history
     * Useful when items are returned or reservation is cancelled
     */
    fun removeReservedItems(studentId: String, items: List<CartItem>) {
        val userReservedItems = reservedItemsMap[studentId] ?: return

        items.forEach { item ->
            val itemKey = "${item.itemId}:${item.itemType}"
            userReservedItems.remove(itemKey)
        }

        if (userReservedItems.isEmpty()) {
            reservedItemsMap.remove(studentId)
        }

        saveReservedItems()
    }
}