package com.example.upang_supply_tracker.Adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.dataclass.CartItem

class CartAdapter(
    private val onQuantityChange: (Int, Int) -> Unit,
    private val onRemoveClick: (Int) -> Unit,
    private val onSizeChange: (Int, String) -> Unit  // Add size change callback
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var items: List<CartItem> = emptyList()
    private var filteredItems: List<CartItem> = emptyList()
    private var currentFilter: String = "All"
    private val sizes = arrayOf("XS", "S", "M", "L", "XL", "XXL")  // Common sizes

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cartItemImage: ImageView = view.findViewById(R.id.cartItemImage)
        val cartItemName: TextView = view.findViewById(R.id.cartItemName)
        val cartItemDepartment: TextView = view.findViewById(R.id.cartItemDepartment)
        val sizeLayout: LinearLayout = view.findViewById(R.id.sizeLayout)
        val sizeSpinner: Spinner = view.findViewById(R.id.sizeSpinner)
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


        val departmentText = if (item.courseName != null && item.courseName.isNotEmpty()) {
            "Department: ${item.departmentName} - ${item.courseName}"
        } else {
            "Department: ${item.departmentName}"
        }
        holder.cartItemDepartment.text = departmentText

        // Handle size spinner visibility based on item type
        if (item.itemType == "UNIFORM") {
            holder.sizeLayout.visibility = View.VISIBLE

            // Create adapter for spinner
            val sizeAdapter = ArrayAdapter(
                holder.itemView.context,
                android.R.layout.simple_spinner_item,
                sizes
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            holder.sizeSpinner.adapter = sizeAdapter

            // Set current size if available
            item.size?.let { currentSize ->
                val sizeIndex = sizes.indexOf(currentSize)
                if (sizeIndex >= 0) {
                    holder.sizeSpinner.setSelection(sizeIndex)
                }
            }

            // Prevent initial automatic callback
            var isInitialSelection = true

            // Set listener for size changes
            holder.sizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, sizePosition: Int, id: Long) {
                    if (isInitialSelection) {
                        isInitialSelection = false
                        return
                    }

                    val newSize = sizes[sizePosition]
                    onSizeChange(item.itemId, newSize)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }
        } else {
            // Hide spinner for non-uniform items
            holder.sizeLayout.visibility = View.GONE
        }

        // Set the default icon based on item type
        when (item.itemType) {
            "BOOK" -> holder.cartItemImage.setImageResource(R.drawable.books)
            "UNIFORM" -> holder.cartItemImage.setImageResource(R.drawable.uniform_icon)
            else -> holder.cartItemImage.setImageResource(R.drawable.module_icon)
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