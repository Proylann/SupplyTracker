package com.example.upang_supply_tracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Uniform : AppCompatActivity() {

    private lateinit var listView: ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_uniform)


        listView = findViewById(R.id.supplyListView)
        val supplyItems = listOf(
            SupplyItem(
                id = 1,
                name = "University Uniform",
                imageResId = R.drawable.polo,
                stockCount = 50,
                description = "Official university uniform"
            ),
            SupplyItem(
                id = 2,
                name = "Math Module",
                imageResId = R.drawable.polo,
                stockCount = 100,
                description = "Mathematics learning module"
            ),
            SupplyItem(
                id = 3,
                name = "Science Book",
                imageResId = R.drawable.polo,
                stockCount = 75,
                description = "Science reference book"
            ),
            SupplyItem(
                id = 4,
            name = "Science Book",
            imageResId = R.drawable.polo,
            stockCount = 75,
            description = "Science reference book"
        ),
        SupplyItem(
            id = 5,
            name = "Science Book",
            imageResId = R.drawable.polo,
            stockCount = 75,
            description = "Science reference book"
        ),
            SupplyItem(
                id = 6,
                name = "Science Book",
                imageResId = R.drawable.polo,
                stockCount = 75,
                description = "Science reference book"
            ),
            SupplyItem(
                id = 7,
                name = "Science Book",
                imageResId = R.drawable.polo,
                stockCount = 75,
                description = "Science reference book"
            )

        )

        val adapter = SupplyAdapter(this, supplyItems)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = supplyItems[position]
            openDetailActivity(selectedItem)
        }
    }

    private fun openDetailActivity(item: SupplyItem) {
        val intent = Intent(this, Details::class.java).apply {
            putExtra("itemId", item.id)
            putExtra("itemName", item.name)
            putExtra("itemStock", item.stockCount)
            putExtra("itemImage", item.imageResId)
            putExtra("itemDescription", item.description)
        }
        startActivity(intent)
    }
}

