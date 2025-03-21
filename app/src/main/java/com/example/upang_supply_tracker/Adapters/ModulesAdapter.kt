package com.example.upang_supply_tracker.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.models.Module
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class ModulesAdapter(
    private val context: Context,
    private var modules: List<Module>,
    private val onItemClick: (Module) -> Unit,
    private val onReserveClick: (Module) -> Unit
) : RecyclerView.Adapter<ModulesAdapter.ModuleViewHolder>() {

    class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.moduleTitle)
        val courseTextView: TextView = itemView.findViewById(R.id.moduleCourse)
        val departmentTextView: TextView = itemView.findViewById(R.id.moduleDepartment)
        val semesterTextView: TextView = itemView.findViewById(R.id.moduleSemester)
        val quantityChip: Chip = itemView.findViewById(R.id.moduleQuantity)
        val reserveButton: MaterialButton = itemView.findViewById(R.id.reserveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.module_item, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]

        holder.titleTextView.text = module.title
        holder.courseTextView.text = "Course: ${module.courseName ?: "N/A"}"
        holder.departmentTextView.text = "Department: ${module.departmentName}"
        holder.semesterTextView.text = "Semester: ${module.semester}"
        holder.quantityChip.text = "Quantity: ${module.quantity}"

        // Set chip color based on quantity
        if (module.quantity > 0) {
            holder.quantityChip.setChipBackgroundColorResource(R.color.success_bg)
            holder.quantityChip.setTextColor(context.getColor(R.color.success_text))
            holder.quantityChip.setChipIconTintResource(R.color.success_icon)
            holder.reserveButton.isEnabled = true
        } else {
            holder.quantityChip.setChipBackgroundColorResource(R.color.success_bg)
            holder.quantityChip.setTextColor(context.getColor(R.color.active))
            holder.quantityChip.setChipIconTintResource(R.color.text_primary)
            holder.reserveButton.isEnabled = false
        }

        // Set click listeners
        holder.itemView.setOnClickListener {
            onItemClick(module)
        }

        holder.reserveButton.setOnClickListener {
            onReserveClick(module)
        }
    }

    override fun getItemCount(): Int {
        return modules.size
    }

    fun updateData(newModules: List<Module>) {
        modules = newModules
        notifyDataSetChanged()
    }

    fun filterBySearch(query: String) {
        if (query.isEmpty()) {
            return
        }
        val filteredList = modules.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.courseName?.contains(query, ignoreCase = true) == true ||
                    it.departmentName.contains(query, ignoreCase = true)
        }
        updateData(filteredList)
    }
}