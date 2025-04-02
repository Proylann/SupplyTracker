package com.example.upang_supply_tracker.Adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.upang_supply_tracker.R;
import com.example.upang_supply_tracker.Services.CartService;
import com.example.upang_supply_tracker.Services.ReservationValidator;
import com.example.upang_supply_tracker.Services.UserManager;
import com.example.upang_supply_tracker.dataclass.Books;
import com.example.upang_supply_tracker.dataclass.CartItem;

class BookRecyclerAdapter(
    private val context: Context,
    private val books: List<Books>,
    private val onItemClick: (Books) -> Unit
) : RecyclerView.Adapter<BookRecyclerAdapter.BookViewHolder>() {

    private val cartService = CartService.getInstance()
    private val userManager = UserManager.getInstance()

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookImage: ImageView = view.findViewById(R.id.bookImage)
        val bookTitle: TextView = view.findViewById(R.id.bookTitle)
        val departmentCourse: TextView = view.findViewById(R.id.departmentCourse)
        val bookAvailability: TextView = view.findViewById(R.id.availabilityText)
        val reserveButton: Button = view.findViewById(R.id.reserveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun getItemCount(): Int = books.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val departmentMap = mapOf(
            "CAHS" to "6",
            "CAS" to "9",
            "CCJE" to "11",
            "CEA" to "2",
            "CELA" to "5",
            "CITE" to "1",
            "CMA" to "10"
        )

        val currentBook = books[position]
        val currentUser = UserManager.getInstance().getCurrentUser()

        val bookDepartmentID = departmentMap[currentBook.Department] ?: "Unknown"
        val userDepartmentID = currentUser?.departmentID ?: "Unknown"

        Log.d(
            "DepartmentCheck",
            "Book: ${currentBook.Department} (ID: $bookDepartmentID), User ID: $userDepartmentID"
        )

        // Set book title
        holder.bookTitle.text = currentBook.BookTitle

        // Set department and course
        holder.departmentCourse.text = "${currentBook.Department} - ${currentBook.Course}"

        // Set availability with color based on quantity
        holder.bookAvailability.text = "Available: ${currentBook.Quantity} copies"

        val isEligible = userDepartmentID == bookDepartmentID

        // Check if the book is already in the cart
        val isInCart = cartService.isItemInCart(currentBook.ID.toInt(), "BOOK")

        Log.d("EligibilityCheck", "isEligible: $isEligible, isInCart: $isInCart")
        if (currentBook.Quantity <= 0) {
            holder.bookAvailability.setTextColor(context.getColor(R.color.status_pending))
            holder.reserveButton.isEnabled = false
            holder.reserveButton.text = "Out of Stock"
            holder.reserveButton.setBackgroundColor(ContextCompat.getColor(context, R.color.status_pending)) // Add this line
        } else if (!isEligible) {
            holder.bookAvailability.setTextColor(context.getColor(R.color.status_pending))
            holder.reserveButton.isEnabled = false
            holder.reserveButton.setBackgroundColor(ContextCompat.getColor(context, R.color.status_pending))
            holder.reserveButton.text = "Not Available for Your Dept."
        } else if (isInCart) {
            holder.bookAvailability.setTextColor(context.getColor(R.color.active))
            holder.reserveButton.setBackgroundColor(ContextCompat.getColor(context, R.color.gray))
            holder.reserveButton.isEnabled = false
            holder.reserveButton.text = "Already in Cart"
        } else {
            holder.bookAvailability.setTextColor(context.getColor(R.color.active))
            holder.reserveButton.isEnabled = true
            holder.reserveButton.text = "Add to Cart"
            holder.reserveButton.setBackgroundColor(ContextCompat.getColor(context, R.color.button)) // Add this line to reset button color
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
            // Handle reservation/add to cart
            if (currentBook.Quantity > 0 && isEligible && !isInCart) {
                // Get current student ID from UserManager
                val studentId = userManager.getCurrentUser()?.studentNumber ?: ""

                // Create cart item
                val cartItem = CartItem(
                    itemId = currentBook.ID.toInt(),
                    name = currentBook.BookTitle,
                    departmentName = currentBook.Department,
                    courseName = currentBook.Course,
                    img = currentBook.Preview,
                    quantity = 1,
                    itemType = "BOOK",
                    size = null  // Books don't have sizes
                )

                // Check if this item can be reserved
                val reservationValidator = ReservationValidator.getInstance()
                if (!reservationValidator.canReserveItem(studentId, cartItem)) {
                    Toast.makeText(context, "You have already reserved this book", Toast.LENGTH_SHORT).show()
                } else {
                    val result = cartService.addToCart(cartItem)  // Use the general method directly
                    if (result) {
                        Toast.makeText(context, "${currentBook.BookTitle} added to cart", Toast.LENGTH_SHORT).show()
                        // Update button state immediately
                        holder.reserveButton.isEnabled = false
                        holder.reserveButton.text = "Already in Cart"
                    } else {
                        Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}