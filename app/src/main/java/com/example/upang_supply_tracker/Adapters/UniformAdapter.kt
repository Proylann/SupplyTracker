package com.example.upang_supply_tracker.Adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.Services.CartService
import com.example.upang_supply_tracker.Services.UserManager
import com.example.upang_supply_tracker.models.CartItem
import com.example.upang_supply_tracker.models.Uniform
import com.google.android.material.button.MaterialButton

class UniformAdapter(
    private var uniforms: List<Uniform> = emptyList(),
    private val onItemClick: (Uniform) -> Unit
) : RecyclerView.Adapter<UniformAdapter.UniformViewHolder>() {

    private val cartService = CartService.getInstance()
    private val userManager = UserManager.getInstance()

    // Mapping department names to IDs
    private val departmentMap = mapOf(
        "CAHS" to "6",
        "CAS" to "9",
        "CCJE" to "11",
        "CEA" to "2",
        "CELA" to "5",
        "CITE" to "1",
        "CMA" to "10"
    )

    class UniformViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uniformImage: ImageView = view.findViewById(R.id.uniformImage)
        val uniformName: TextView = view.findViewById(R.id.uniformName)
        val uniformDepartment: TextView = view.findViewById(R.id.uniformDepartment)
        val quantityText: TextView = view.findViewById(R.id.quantityText)
        val reserveButton: MaterialButton = view.findViewById(R.id.reserveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniformViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_uniform, parent, false)
        return UniformViewHolder(view)
    }

    override fun onBindViewHolder(holder: UniformViewHolder, position: Int) {
        val uniform = uniforms[position]
        val context = holder.itemView.context

        val currentUser = userManager.getCurrentUser()
        val userDepartmentID = currentUser?.departmentID ?: "Unknown"

        val uniformDepartmentID = departmentMap[uniform.departmentName] ?: "Unknown"

        Log.d("DepartmentCheck", "Uniform: ${uniform.departmentName} (ID: $uniformDepartmentID), User ID: $userDepartmentID")

        // Check if user can reserve this uniform
        val isEligible = userDepartmentID == uniformDepartmentID
        Log.d("EligibilityCheck", "isEligible: $isEligible")

        // Set text values
        holder.uniformName.text = uniform.name
        holder.uniformDepartment.text = "Department: ${uniform.departmentName}"

        // Set image if available
        if (!uniform.img.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(uniform.img, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.uniformImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                holder.uniformImage.setImageResource(R.drawable.uniform_icon)
            }
        } else {
            holder.uniformImage.setImageResource(R.drawable.uniform_icon)
        }

        // Set availability and eligibility status
        when {
            uniform.quantity <= 0 -> {
                holder.quantityText.text = "Out of Stock"
                holder.quantityText.setTextColor(context.getColor(R.color.status_pending)) // Red color
                holder.reserveButton.isEnabled = false
            }
            !isEligible -> {
                holder.quantityText.text = "In Stock: ${uniform.quantity}"
                holder.quantityText.setTextColor(context.getColor(R.color.status_pending)) // Red color
                holder.reserveButton.isEnabled = false
                holder.reserveButton.text = "Not Available for Your Dept."
                holder.reserveButton.setBackgroundColor(context.getColor(R.color.status_pending))
            }
            else -> {
                holder.quantityText.text = "In Stock: ${uniform.quantity}"
                holder.quantityText.setTextColor(context.getColor(R.color.active)) // Default color
                holder.reserveButton.isEnabled = true
                holder.reserveButton.text = "Add to Cart"
                holder.reserveButton.setBackgroundColor(context.getColor(R.color.button))
            }
        }

        // Handle add to cart
        holder.reserveButton.setOnClickListener {
            if (isEligible) {
                val cartItem = CartItem(
                    itemId = uniform.uniformId,
                    name = uniform.name,
                    description = "",
                    departmentName = uniform.departmentName,
                    courseName = uniform.courseName,
                    img = uniform.img,
                    quantity = 1,
                    itemType = "UNIFORM"
                )

                cartService.addToCart(cartItem)
                Toast.makeText(context, "${uniform.name} added to cart", Toast.LENGTH_SHORT).show()
                onItemClick(uniform)
            }
        }
    }

    override fun getItemCount() = uniforms.size

    fun updateData(newUniforms: List<Uniform>) {
        uniforms = newUniforms
        notifyDataSetChanged()
    }
}
