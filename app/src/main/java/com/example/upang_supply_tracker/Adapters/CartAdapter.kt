package com.example.upang_supply_tracker.Adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.models.CartItem

class CartAdapter(
    private val onQuantityChange: (Int, Int) -> Unit,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var items: List<CartItem> = emptyList()
    private var filteredItems: List<CartItem> = emptyList()
    private var currentFilter: String = "All"

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cartItemImage: ImageView = view.findViewById(R.id.cartItemImage)
        val cartItemName: TextView = view.findViewById(R.id.cartItemName)
        val cartItemDescription: TextView = view.findViewById(R.id.cartItemDescription)
        val cartItemDepartment: TextView = view.findViewById(R.id.cartItemDepartment)
        val btnRemove: Button = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int = filteredItems.size


    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = filteredItems[position]

        holder.cartItemName.text = item.name
        holder.cartItemDescription.text = item.description

        val departmentText = if (item.courseName != null && item.courseName.isNotEmpty()) {
            "Department: ${item.departmentName} - ${item.courseName}"
        } else {
            "Department: ${item.departmentName}"
        }
        holder.cartItemDepartment.text = departmentText



        // Set the default icon based on item type
        when (item.itemType) {
            "BOOK" -> holder.cartItemImage.setImageResource(R.drawable.books)
            "UNIFORM" -> holder.cartItemImage.setImageResource(R.drawable.uniform_icon)
            else -> holder.cartItemImage.setImageResource(R.drawable.module_icon) // Assuming you have a module icon
        }

        // Try to load image if available
        if (!item.img.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(item.img, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.cartItemImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // Keep default image if decoding fails
            }
        }

        // Set button click listeners

        holder.btnRemove.setOnClickListener {
            onRemoveClick(item.itemId)
        }
    }




    fun updateData(newItems: List<CartItem>) {
        items = newItems
        applyFilter(currentFilter)
    }

    fun applyFilter(filter: String) {
        currentFilter = filter
        filteredItems = when (filter) {
            "Books" -> items.filter { it.itemType == "BOOK" }
            "Uniforms" -> items.filter { it.itemType == "UNIFORM" }
            "Modules" -> items.filter { it.itemType == "MODULE" }
            else -> items
        }
        notifyDataSetChanged()
    }
}