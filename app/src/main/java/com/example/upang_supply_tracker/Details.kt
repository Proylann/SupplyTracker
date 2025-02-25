package com.example.upang_supply_tracker

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Details : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Get data from intent
        val itemId = intent.getIntExtra("itemId", -1)
        val itemName = intent.getStringExtra("itemName") ?: ""
        val itemStock = intent.getIntExtra("itemStock", 0)
        val itemImage = intent.getIntExtra("itemImage", 0)
        val itemDescription = intent.getStringExtra("itemDescription") ?: ""

        // Set up the views
        findViewById<ImageView>(R.id.detailImage).setImageResource(itemImage)
        findViewById<TextView>(R.id.detailName).text = itemName
        findViewById<TextView>(R.id.detailStock).text = "Available: $itemStock items"
        findViewById<TextView>(R.id.detailDescription).text = itemDescription

        // Set up the reserve button action
        findViewById<Button>(R.id.reserveButton).setOnClickListener {
//            // Navigate to reservation process
//            openReservationScreen(itemId, itemName)
        }

        // Add back button to action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Item Details"
    }

//    private fun openReservationScreen(itemId: Int, itemName: String) {
//        val intent = Intent(this, ReservationActivity::class.java).apply {
//            putExtra("itemId", itemId)
//            putExtra("itemName", itemName)
//        }
//        startActivity(intent)
//    }

    // Handle back button in action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}