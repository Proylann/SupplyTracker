package com.example.upang_supply_tracker.Adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.dataclass.Books

class BookRecyclerAdapter(
    private val context: Context,
    private val books: List<Books>,
    private val onItemClick: (Books) -> Unit
) : RecyclerView.Adapter<BookRecyclerAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookImage: ImageView = view.findViewById(R.id.bookImage)
        val bookTitle: TextView = view.findViewById(R.id.bookTitle)
        val departmentCourse: TextView = view.findViewById(R.id.departmentCourse)
        val bookAvailability: TextView = view.findViewById(R.id.availabilityChip)
        val reserveButton: Button = view.findViewById(R.id.reserveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false)
        return BookViewHolder(view)
    }

    override fun getItemCount(): Int = books.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val currentBook = books[position]

        // Set book title
        holder.bookTitle.text = currentBook.BookTitle

        // Set department and course
        holder.departmentCourse.text = "${currentBook.Department} - ${currentBook.Course}"

        // Set availability with color based on quantity
        holder.bookAvailability.text = "Available: ${currentBook.Quantity} copies"
        if (currentBook.Quantity > 0) {
            holder.bookAvailability.setTextColor(context.getColor(R.color.active))
            holder.reserveButton.isEnabled = true
        } else {
            holder.bookAvailability.setTextColor(context.getColor(R.color.black))
            holder.reserveButton.isEnabled = false
        }

        // Handle the book preview image
        if (!currentBook.Preview.isNullOrEmpty()) {
            try {
                // Convert Base64 to Bitmap
                val imageBytes = Base64.decode(currentBook.Preview, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.bookImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // Set default image if decoding fails
                holder.bookImage.setImageResource(R.drawable.books)
            }
        } else {
            // Set default image if no preview available
            holder.bookImage.setImageResource(R.drawable.books)
        }

        // Set click listeners
        holder.itemView.setOnClickListener { onItemClick(currentBook) }
        holder.reserveButton.setOnClickListener {
            // Handle reservation
            if (currentBook.Quantity > 0) {
                onItemClick(currentBook)
            }
        }
    }
}