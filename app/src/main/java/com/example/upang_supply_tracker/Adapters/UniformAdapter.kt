package com.example.upang_supply_tracker.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.Services.CartService
import com.example.upang_supply_tracker.models.CartItem
import com.example.upang_supply_tracker.models.Uniform
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class UniformAdapter(
    private var uniforms: List<Uniform> = emptyList(),
    private val onItemClick: (Uniform) -> Unit
) : RecyclerView.Adapter<UniformAdapter.UniformViewHolder>() {

    private val cartService = CartService.getInstance()

    class UniformViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uniformImage: ImageView = view.findViewById(R.id.uniformImage)
        val uniformName: TextView = view.findViewById(R.id.uniformName)
        val uniformDescription: TextView = view.findViewById(R.id.uniformDescription)
        val uniformDepartment: TextView = view.findViewById(R.id.uniformDepartment)
        val quantityChip: Chip = view.findViewById(R.id.quantity)
        val reserveButton: MaterialButton = view.findViewById(R.id.reserveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniformViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_uniform, parent, false)
        return UniformViewHolder(view)
    }

    override fun onBindViewHolder(holder: UniformViewHolder, position: Int) {
        val uniform = uniforms[position]

        // Set text values
        holder.uniformName.text = uniform.name
        holder.uniformDescription.text = uniform.description

        // Set department and course info
        val departmentInfo = if (uniform.courseName != null) {
            "Department: ${uniform.departmentName}\nCourse: ${uniform.courseName}"
        } else {
            "Department: ${uniform.departmentName}"
        }
        holder.uniformDepartment.text = departmentInfo

        // Set quantity chip
        holder.quantityChip.text = "Quantity: ${uniform.quantity}"

        // Set image if available
        if (!uniform.img.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(uniform.img, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.uniformImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // If there's an error decoding the image, use the default
                holder.uniformImage.setImageResource(R.drawable.uniform_icon)
            }
        } else {
            // No image, use default
            holder.uniformImage.setImageResource(R.drawable.uniform_icon)
        }

        // Change button text to "Add to Cart" instead of "Reserve"
        holder.reserveButton.text = "Add to Cart"

        // Set button click listener - now adds to cart
        holder.reserveButton.setOnClickListener {
            // Create a CartItem from the Uniform
            val cartItem = CartItem(
                itemId = uniform.uniformId,
                name = uniform.name,
                description = uniform.description,
                departmentName = uniform.departmentName,
                courseName = uniform.courseName,
                img = uniform.img,
                quantity = 1, // Default quantity is 1
                itemType = "UNIFORM" // Set the item type to UNIFORM
            )

            // Add the CartItem to cart
            cartService.addToCart(cartItem)

            // Show a toast notification
            Toast.makeText(
                holder.itemView.context,
                "${uniform.name} added to cart",
                Toast.LENGTH_SHORT
            ).show()

            // Also call the original onItemClick if needed
            onItemClick(uniform)
        }

        // Set availability status
        if (uniform.quantity <= 0) {
            holder.quantityChip.text = "Out of Stock"
            holder.quantityChip.setChipBackgroundColorResource(R.color.button_primary_pressed)
            holder.quantityChip.setTextColor(holder.itemView.context.getColor(R.color.active))
            holder.reserveButton.isEnabled = false
        } else {
            holder.reserveButton.isEnabled = true
        }
    }

    override fun getItemCount() = uniforms.size

    fun updateData(newUniforms: List<Uniform>) {
        uniforms = newUniforms
        notifyDataSetChanged()
    }
}