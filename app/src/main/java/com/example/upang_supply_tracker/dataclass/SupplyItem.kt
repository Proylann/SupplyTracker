package com.example.upang_supply_tracker.dataclass

data class SupplyItem(
    val id: Int,
    val name: String,
    val imageResId: Int,
    val stockCount: Int,
    val description: String,

)
