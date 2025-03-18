package com.example.upang_supply_tracker.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.dataclass.SupplyItem

class SupplyAdapter(
    private val context: Context,
    private val items: List<SupplyItem>
) : ArrayAdapter<SupplyItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context)
                .inflate(R.layout.supply_list, parent, false)
        }

        val currentItem = items[position]

        itemView?.apply {
            findViewById<ImageView>(R.id.itemImage).setImageResource(currentItem.imageResId)
            findViewById<TextView>(R.id.itemName).text = currentItem.name
            findViewById<TextView>(R.id.stockCount).text =
                "Available: ${currentItem.stockCount} items"
        }

        return itemView!!
    }
}