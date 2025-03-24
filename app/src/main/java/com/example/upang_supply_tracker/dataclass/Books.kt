package com.example.upang_supply_tracker.dataclass

data class Books(
    val ID: String,
    val Preview: String?,
    val BookTitle: String,
    val Department: String,
    val Course: String,
    val Quantity: Int
)


data class BookResponse(
    val books: List<Books>
)