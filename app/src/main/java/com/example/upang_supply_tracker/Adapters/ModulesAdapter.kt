package com.example.upang_supply_tracker.Adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.example.upang_supply_tracker.R;
import com.example.upang_supply_tracker.Services.CartService;
import com.example.upang_supply_tracker.Services.ReservationValidator;
import com.example.upang_supply_tracker.Services.UserManager;
import com.example.upang_supply_tracker.dataclass.CartItem;
import com.example.upang_supply_tracker.models.Module;
import com.google.android.material.button.MaterialButton;

class ModulesAdapter(
    private val context: Context,
    private var modules: List<Module>,
    private val onItemClick: (Module) -> Unit
) : RecyclerView.Adapter<ModulesAdapter.ModuleViewHolder>() {

    private val cartService = CartService.getInstance()
    private val userManager = UserManager.getInstance()
    private var allModules: List<Module> = modules // Store the original list

    class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val moduleImageView: ImageView = itemView.findViewById(R.id.moduleImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.moduleTitle)
        val courseTextView: TextView = itemView.findViewById(R.id.moduleCourse)
        val departmentTextView: TextView = itemView.findViewById(R.id.moduleDepartment)
        val semesterTextView: TextView = itemView.findViewById(R.id.moduleSemester)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantity)
        val reserveButton: MaterialButton = itemView.findViewById(R.id.reserveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]
        val currentUser = userManager.getCurrentUser()

        val departmentMap = mapOf(
            "CAHS" to "6",
            "CAS" to "9",
            "CCJE" to "11",
            "CEA" to "2",
            "CELA" to "5",
            "CITE" to "1",
            "CMA" to "10"
        )

        val moduleDepartmentID = departmentMap[module.Name] ?: "Unknown"
        val userDepartmentID = currentUser?.departmentID ?: "Unknown"

        val isEligible = userDepartmentID == moduleDepartmentID

        // Check if module is already in cart
        val isInCart = cartService.isItemInCart(module.moduleId, "MODULE")

        // Set basic module information
        holder.titleTextView.text = module.title
        holder.courseTextView.text = "Course: ${module.courseName ?: "N/A"}"
        holder.departmentTextView.text = "Department: ${module.Name}"
        holder.semesterTextView.text = "Semester: ${module.semester}"
        holder.quantityTextView.text = "Quantity: ${module.quantity}"

        // Set module image from Base64 string
        if (!module.imageData.isNullOrEmpty()) {
            try {
                // Convert Base64 to Bitmap
                val imageBytes = Base64.decode(module.imageData, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.moduleImageView.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // Set default image if decoding fails
                holder.moduleImageView.setImageResource(R.drawable.module_icon)
            }
        } else {
            // Set default image if no image data available
            holder.moduleImageView.setImageResource(R.drawable.module_icon)
        }

        // Set availability color and button state
        if (module.quantity <= 0) {
            holder.quantityTextView.setTextColor(context.getColor(R.color.status_pending))
            holder.reserveButton.setBackgroundColor(context.getColor(R.color.text_secondary))
            holder.reserveButton.isEnabled = false
            holder.reserveButton.text = "Out of Stock"
        } else if (!isEligible) {
            holder.quantityTextView.setTextColor(context.getColor(R.color.status_pending)) // Red color
            holder.reserveButton.isEnabled = false
            holder.reserveButton.text = "Not Available for Your Dept."
            holder.reserveButton.setBackgroundColor(context.getColor(R.color.status_pending))
        } else if (isInCart) {
            holder.quantityTextView.setTextColor(context.getColor(R.color.active))
            holder.reserveButton.setBackgroundColor(context.getColor(R.color.gray))
            holder.reserveButton.isEnabled = false
            holder.reserveButton.text = "Already in Cart"
        } else {
            holder.quantityTextView.setTextColor(context.getColor(R.color.active))
            holder.reserveButton.isEnabled = true
            holder.reserveButton.text = "Add to Cart"
        }

        holder.itemView.setOnClickListener {
            onItemClick(module)
        }

        holder.reserveButton.setOnClickListener {
            if (module.quantity > 0 && isEligible && !isInCart) {
                val cartItem = CartItem(
                    itemId = module.moduleId,
                    name = module.title,
                    departmentName = module.Name ?: "",
                    courseName = module.courseName ?: "",
                    img = module.imageData, // Pass the image data to the cart
                    quantity = 1,
                    itemType = "MODULE",
                    size = null  // Since modules don't have sizes, use null
                )

                // Get current student ID from UserManager
                val studentId = userManager.getCurrentUser()?.studentNumber ?: ""

                // Check if this item can be reserved
                val reservationValidator = ReservationValidator.getInstance()
                if (!reservationValidator.canReserveItem(studentId, cartItem)) {
                    Toast.makeText(
                        context,
                        "You have already reserved this module",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val result = cartService.addToCart(cartItem)
                    if (result) {
                        Toast.makeText(context, "${module.title} added to cart", Toast.LENGTH_SHORT).show()
                        // Update button state immediately
                        holder.reserveButton.isEnabled = false
                        holder.reserveButton.text = "Already in Cart"
                    } else {
                        Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Cannot add module to cart", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return modules.size
    }

    fun updateData(newModules: List<Module>) {
        allModules = newModules // Update the original list
        modules = newModules
        notifyDataSetChanged()
    }

    fun filterBySearch(query: String) {
        modules = if (query.isEmpty()) {
            allModules // Restore full list when search is cleared
        } else {
            allModules.filter { module ->
                module.title.contains(query, ignoreCase = true) ||
                        (module.courseName ?: "").contains(query, ignoreCase = true) ||
                        (module.Name ?: "").contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}