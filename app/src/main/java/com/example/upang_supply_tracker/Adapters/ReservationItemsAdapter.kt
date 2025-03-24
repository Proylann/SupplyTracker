package com.example.upang_supply_tracker.Adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.models.CartItem

class ReservationItemsAdapter(
    private val context: Context,
    private val items: List<CartItem>
) : RecyclerView.Adapter<ReservationItemsAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgItem: ImageView = view.findViewById(R.id.img_item)
        val txtItemName: TextView = view.findViewById(R.id.txt_item_name)
        val txtItemDetails: TextView = view.findViewById(R.id.txt_item_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reservation_item_details, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]

        // Set item name
        holder.txtItemName.text = item.name

        // Set item details
        holder.txtItemDetails.text = "Quantity: ${item.quantity} | Type: ${item.itemType}"

        // Load image
        if (!item.img.isNullOrEmpty()) {
            try {
                // First try to decode as Base64
                val imageBytes = Base64.decode(item.img, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.imgItem.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // If Base64 decoding fails, try loading as URL with Glide
                Glide.with(context)
                    .load(item.img)
                    .placeholder(R.drawable.empty_cart_icon)
                    .error(R.drawable.empty_cart_icon)
                    .into(holder.imgItem)
            }
        } else {
            // Set default image based on item type
            val defaultImageRes = when (item.itemType) {
                "BOOK" -> R.drawable.books_icon
                "MODULE" -> R.drawable.module_icon
                "UNIFORM" -> R.drawable.uniform_icon
                else -> R.drawable.empty_cart_icon
            }
            holder.imgItem.setImageResource(defaultImageRes)
        }
    }
}