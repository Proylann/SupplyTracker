package com.example.upang_supply_tracker.Adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.models.CartItem
import com.google.android.material.imageview.ShapeableImageView

class CartAdapter(
    private val onQuantityChange: (uniformId: Int, quantity: Int) -> Unit,
    private val onRemoveClick: (uniformId: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var cartItems: List<CartItem> = emptyList()
    private var filteredItems: List<CartItem> = emptyList()
    private var currentFilter: String = "All"

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ShapeableImageView = view.findViewById(R.id.cartItemImage)
        val itemName: TextView = view.findViewById(R.id.cartItemName)
        val itemDescription: TextView = view.findViewById(R.id.cartItemDescription)
        val itemDepartment: TextView = view.findViewById(R.id.cartItemDepartment)
        val itemQuantity: TextView = view.findViewById(R.id.quantityText)
        val btnIncrease: ImageButton = view.findViewById(R.id.btnIncrease)
        val btnDecrease: ImageButton = view.findViewById(R.id.btnDecrease)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = filteredItems[position]

        // Set text values
        holder.itemName.text = cartItem.name
        holder.itemDescription.text = cartItem.description

        // Set department and course info
        val departmentInfo = if (cartItem.courseName != null) {
            "Department: ${cartItem.departmentName}\nCourse: ${cartItem.courseName}"
        } else {
            "Department: ${cartItem.departmentName}"
        }
        holder.itemDepartment.text = departmentInfo

        // Set quantity
        holder.itemQuantity.text = cartItem.quantity.toString()

        // Set image if available
        if (!cartItem.img.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(cartItem.img, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.itemImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // If there's an error decoding the image, use the default
                holder.itemImage.setImageResource(R.drawable.uniform_icon)
            }
        } else {
            // No image, use default
            holder.itemImage.setImageResource(R.drawable.uniform_icon)
        }

        // Set button click listeners
        holder.btnIncrease.setOnClickListener {
            onQuantityChange(cartItem.uniformId, cartItem.quantity + 1)
        }

        holder.btnDecrease.setOnClickListener {
            if (cartItem.quantity > 1) {
                onQuantityChange(cartItem.uniformId, cartItem.quantity - 1)
            }
        }

        holder.btnRemove.setOnClickListener {
            onRemoveClick(cartItem.uniformId)
        }
    }

    override fun getItemCount() = filteredItems.size

    fun updateData(newCartItems: List<CartItem>) {
        cartItems = newCartItems
        applyFilter(currentFilter)
        notifyDataSetChanged()
    }

    fun applyFilter(filter: String) {
        currentFilter = filter

        filteredItems = when (filter) {
            "Books" -> cartItems.filter { it.name.contains("Book", ignoreCase = true) }
            "Uniforms" -> cartItems.filter { it.name.contains("Uniform", ignoreCase = true) }
            "Modules" -> cartItems.filter { it.name.contains("Module", ignoreCase = true) }
            else -> cartItems  // "All" or any other value
        }

        notifyDataSetChanged()
    }
}