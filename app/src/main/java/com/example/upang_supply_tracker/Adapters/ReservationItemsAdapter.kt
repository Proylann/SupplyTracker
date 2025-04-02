package com.example.upang_supply_tracker.Adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.dataclass.CartItem

class ReservationItemsAdapter(
    private val context: Context,
    private val items: List<CartItem>
) : RecyclerView.Adapter<ReservationItemsAdapter.ItemViewHolder>() {

    companion object {
        private const val TAG = "ReservationItemsAdapter"
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgItem: ImageView = view.findViewById(R.id.img_item)
        val txtItemName: TextView = view.findViewById(R.id.txt_item_name)
        val txtItemDetails: TextView = view.findViewById(R.id.txt_item_details)
        val txtItemSize: TextView = view.findViewById(R.id.txt_item_size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reservation_item_details, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]

        holder.txtItemName.text = item.name
        holder.txtItemDetails.text = "Quantity: ${item.quantity} | Type: ${item.itemType}"

        // Handle size display for uniforms
        if (item.itemType == "UNIFORM" && !item.size.isNullOrEmpty()) {
            holder.txtItemSize.visibility = View.VISIBLE
            holder.txtItemSize.text = "Size: ${item.size}"
            Log.d(TAG, "Showing uniform size: ${item.size}")
        } else {
            holder.txtItemSize.visibility = View.GONE
        }

        // Load image with better error handling
        loadItemImage(holder.imgItem, item)
    }

    private fun loadItemImage(imageView: ImageView, item: CartItem) {
        val imageData = item.img

        if (!imageData.isNullOrEmpty()) {
            Log.d(TAG, "Loading image for ${item.name}, type: ${item.itemType}")

            try {
                // Handle data URL format (e.g., "data:image/jpeg;base64,...")
                if (imageData.startsWith("data:")) {
                    val base64Data = imageData.substring(imageData.indexOf(",") + 1)
                    try {
                        val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                        if (decodedImage != null) {
                            imageView.setImageBitmap(decodedImage)
                            Log.d(TAG, "Successfully loaded base64 image from data URL")
                            return
                        } else {
                            Log.w(TAG, "Failed to decode bitmap from data URL")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error decoding data URL: ${e.message}")
                    }
                }
                // Handle plain base64 data
                else if (imageData.length > 100 && !imageData.startsWith("http")) {
                    try {
                        val imageBytes = Base64.decode(imageData, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                        if (decodedImage != null) {
                            imageView.setImageBitmap(decodedImage)
                            Log.d(TAG, "Successfully loaded plain base64 image")
                            return
                        } else {
                            Log.w(TAG, "Failed to decode bitmap from plain base64")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error decoding base64: ${e.message}")
                    }
                }

                // Fall back to Glide for URLs or if decoding failed
                Glide.with(context)
                    .load(imageData)
                    .placeholder(R.drawable.empty_cart_icon)
                    .error(R.drawable.empty_cart_icon)
                    .into(imageView)

                Log.d(TAG, "Attempted to load image with Glide")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image: ${e.message}")
                setDefaultImage(imageView, item.itemType)
            }
        } else {
            Log.d(TAG, "No image data available for ${item.name}")
            setDefaultImage(imageView, item.itemType)
        }
    }

    private fun setDefaultImage(imageView: ImageView, itemType: String) {
        val defaultImageRes = when (itemType) {
            "BOOK" -> R.drawable.books_icon
            "MODULE" -> R.drawable.module_icon
            "UNIFORM" -> R.drawable.uniform_icon
            else -> R.drawable.empty_cart_icon
        }
        imageView.setImageResource(defaultImageRes)
    }
}