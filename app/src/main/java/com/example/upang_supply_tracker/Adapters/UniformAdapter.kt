package com.example.upang_supply_tracker.Adapters;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.example.upang_supply_tracker.R;
import com.example.upang_supply_tracker.Services.CartService;
import com.example.upang_supply_tracker.Services.ReservationValidator;
import com.example.upang_supply_tracker.Services.UserManager;
import com.example.upang_supply_tracker.dataclass.CartItem;
import com.example.upang_supply_tracker.models.Uniform;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

class UniformAdapter(
    private var uniforms: List<Uniform> = emptyList(),
    private val onItemClick: (Uniform) -> Unit
) : RecyclerView.Adapter<UniformAdapter.UniformViewHolder>() {

    private val cartService = CartService.getInstance()
    private val userManager = UserManager.getInstance()

    // Available sizes
    private val sizes = listOf("S", "M", "L", "XL", "XXL")

    // Mapping department names to IDs
    private val departmentMap = mapOf(
        "CAHS" to "6",
        "CAS" to "9",
        "CCJE" to "11",
        "CEA" to "2",
        "CELA" to "5",
        "CITE" to "1",
        "CMA" to "10",
        "For All Student" to "universal"
    )

    class UniformViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val uniformImage: ImageView = view.findViewById(R.id.uniformImage)
        val uniformName: TextView = view.findViewById(R.id.uniformName)
        val uniformDepartment: TextView = view.findViewById(R.id.uniformDepartment)
        val sizeSpinner: MaterialAutoCompleteTextView = view.findViewById(R.id.sizeSpinner)
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

        val uniformDepartmentID =
            departmentMap[uniform.departmentName] ?: uniform.departmentId.toString()

        Log.d(
            "DepartmentCheck",
            "Uniform: ${uniform.departmentName} (ID: $uniformDepartmentID), User ID: $userDepartmentID"
        )

        // Check if user can reserve this uniform
        val isEligible =
            userDepartmentID == uniformDepartmentID || uniform.departmentName == "For All Student"
        Log.d("EligibilityCheck", "isEligible: $isEligible")

        // Check if the uniform is already in the cart
        val isInCart = cartService.isItemInCart(uniform.uniformId, "UNIFORM")

        // Set text values
        holder.uniformName.text = uniform.name
        holder.uniformDepartment.text = "Department: ${uniform.departmentName}"

        // Setup size spinner
        val sizeAdapter = ArrayAdapter(context, R.layout.sizes_item, sizes)
        sizeAdapter.setDropDownViewResource(R.layout.sizes_item)
        holder.sizeSpinner.setAdapter(sizeAdapter)

        // Set default size if available from uniform
        if (uniform.size?.isNotEmpty() == true && sizes.contains(uniform.size)) {
            holder.sizeSpinner.setText(uniform.size, false)
        } else {
            holder.sizeSpinner.setText(sizes[0], false)
        }

        // Enable/disable spinner based on eligibility and not being in cart
        holder.sizeSpinner.isEnabled = isEligible && uniform.quantity > 0 && !isInCart

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

        // Set availability, eligibility status, and cart status
        when {
            uniform.quantity <= 0 -> {
                holder.quantityText.text = "Out of Stock"
                holder.quantityText.setTextColor(context.getColor(R.color.status_pending)) // Red color
                holder.reserveButton.isEnabled = false
                holder.reserveButton.text = "Out of Stock"
            }

            !isEligible -> {
                holder.quantityText.text = "In Stock: ${uniform.quantity}"
                holder.quantityText.setTextColor(context.getColor(R.color.status_pending)) // Red color
                holder.reserveButton.isEnabled = false
                holder.reserveButton.text = "Not Available for Your Dept."
                holder.reserveButton.setBackgroundColor(context.getColor(R.color.status_pending))
            }

            isInCart -> {
                holder.quantityText.text = "In Stock: ${uniform.quantity}"
                holder.quantityText.setTextColor(context.getColor(R.color.active)) // Default color
                holder.reserveButton.isEnabled = false
                holder.reserveButton.text = "Already in Cart"
                holder.reserveButton.setBackgroundColor(context.getColor(R.color.gray))
            }

            else -> {
                holder.quantityText.text = "In Stock: ${uniform.quantity}"
                holder.quantityText.setTextColor(context.getColor(R.color.active)) // Default color
                holder.reserveButton.isEnabled = true
                holder.reserveButton.text = "Add to Cart"
                holder.reserveButton.setBackgroundColor(context.getColor(R.color.button))
            }
        }

        holder.reserveButton.setOnClickListener {
            if (isEligible && !isInCart) {
                val selectedSize = holder.sizeSpinner.text.toString()

                val cartItem = CartItem(
                    itemId = uniform.uniformId,
                    name = uniform.name,
                    departmentName = uniform.departmentName,
                    courseName = uniform.courseName,
                    img = uniform.img,
                    quantity = 1,
                    itemType = "UNIFORM",
                    size = selectedSize
                )

                // Get current student ID from UserManager
                val studentId = userManager.getCurrentUser()?.studentNumber ?: ""

                // Check if this item can be reserved
                val reservationValidator = ReservationValidator.getInstance()
                if (!reservationValidator.canReserveItem(studentId, cartItem)) {
                    Toast.makeText(
                        context,
                        "You have already reserved this item",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val result = cartService.addToCart(cartItem)
                    if (result) {
                        Toast.makeText(context, "${uniform.name} added to cart", Toast.LENGTH_SHORT).show()
                        // Update the button state immediately after adding to cart
                        holder.reserveButton.isEnabled = false
                        holder.reserveButton.text = "Already in Cart"
                        holder.reserveButton.setBackgroundColor(context.getColor(R.color.gray))
                        holder.sizeSpinner.isEnabled = false
                        onItemClick(uniform)
                    } else {
                        Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun getItemCount() = uniforms.size

    fun updateData(newUniforms: List<Uniform>) {
        uniforms = newUniforms
        notifyDataSetChanged()
    }
}