package com.example.upang_supply_tracker.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.Services.CartService
import com.example.upang_supply_tracker.Services.UserManager
import com.example.upang_supply_tracker.models.CartItem
import com.example.upang_supply_tracker.models.Module
import com.google.android.material.button.MaterialButton

class ModulesAdapter(
    private val context: Context,
    private var modules: List<Module>,
    private val onItemClick: (Module) -> Unit
) : RecyclerView.Adapter<ModulesAdapter.ModuleViewHolder>() {

    private val cartService = CartService.getInstance()
    private val userManager = UserManager.getInstance()
    private var allModules: List<Module> = modules // Store the original list

    class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        holder.titleTextView.text = module.title
        holder.courseTextView.text = "Course: ${module.courseName ?: "N/A"}"
        holder.departmentTextView.text = "Department: ${module.Name}"
        holder.semesterTextView.text = "Semester: ${module.semester}"

        holder.quantityTextView.text = "Quantity: ${module.quantity}"

        if (module.quantity > 0 && isEligible) {
            holder.quantityTextView.setTextColor(context.getColor(R.color.active))
            holder.reserveButton.isEnabled = true
        } else {
            holder.quantityTextView.setTextColor(context.getColor(R.color.status_pending))
            holder.reserveButton.isEnabled = false
        }

        holder.itemView.setOnClickListener {
            onItemClick(module)
        }

        holder.reserveButton.setOnClickListener {
            val cartItem = CartItem(
                itemId = module.moduleId,
                name = module.title,
                description = "${module.Name} module",
                departmentName = module.Name ?: "",
                courseName = module.courseName ?: "",
                img = null,
                quantity = 1,
                itemType = "MODULE"
            )

            if (module.quantity > 0 && isEligible) {
                cartService.addToCart(cartItem)
                Toast.makeText(context, "${module.title} added to cart", Toast.LENGTH_SHORT).show()
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
