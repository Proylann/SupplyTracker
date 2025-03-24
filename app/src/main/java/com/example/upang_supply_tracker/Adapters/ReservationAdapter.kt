package com.example.upang_supply_tracker.Adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.models.Reservation
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

class ReservationsAdapter(
    private val context: Context,
    private val onItemClick: (Reservation) -> Unit
) : RecyclerView.Adapter<ReservationsAdapter.ReservationViewHolder>() {

    private var items: List<Reservation> = emptyList()
    private var filteredItems: List<Reservation> = emptyList()
    private var currentFilter: String = "All"

    // Date formatter
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Status colors
    private val colorToBeClaimed = ContextCompat.getColor(context, R.color.status_to_be_claimed) // Green
    private val colorPending = ContextCompat.getColor(context, R.color.status_pending) // Red
    private val colorProcessing = ContextCompat.getColor(context, R.color.status_processing)
    private val Claimed = ContextCompat.getColor(context, R.color.status_claimed) // Green
    // Orange
    private val colorDefault = ContextCompat.getColor(context, R.color.status_default) // Gray

    class ReservationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reservationId: TextView = view.findViewById(R.id.txt_reservation_id)
        val reservationDate: TextView = view.findViewById(R.id.txt_reservation_date)
        val status: TextView = view.findViewById(R.id.txt_status)
        val itemsSummary: TextView = view.findViewById(R.id.txt_items_summary)
        val viewDetailsButton: MaterialButton = view.findViewById(R.id.btn_view_details)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reservation_items, parent, false)
        return ReservationViewHolder(view)
    }

    override fun getItemCount(): Int = filteredItems.size

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = filteredItems[position]

        // Set reservation ID and date
        holder.reservationId.text = "Reservation #${reservation.id}"

        try {
            val date = inputFormat.parse(reservation.date)
            holder.reservationDate.text = date?.let { outputFormat.format(it) } ?: reservation.date
        } catch (e: Exception) {
            holder.reservationDate.text = reservation.date
        }

        // Set status with appropriate color
        holder.status.text = reservation.status.uppercase()
        val statusBackground = holder.status.background as? GradientDrawable

        // Set color based on status
        val statusColor = when {
            reservation.status.equals("To be claimed", ignoreCase = true) -> colorToBeClaimed // Green
            reservation.status.equals("Pending", ignoreCase = true) -> colorPending // Red
            reservation.status.equals("Processing", ignoreCase = true) -> colorProcessing // Orange
            reservation.status.equals("Claimed", ignoreCase = true) -> Claimed
            else -> colorDefault // Gray
        }

        statusBackground?.setColor(statusColor)

        // Set items summary
        holder.itemsSummary.text = reservation.getItemsSummary()

        // Set click listener for view details button
        holder.viewDetailsButton.setOnClickListener {
            onItemClick(reservation)
        }

        // Make the entire item clickable
        holder.itemView.setOnClickListener {
            onItemClick(reservation)
        }
    }

    fun updateData(newItems: List<Reservation>) {
        items = newItems
        applyFilter(currentFilter)
    }

    fun applyFilter(filter: String) {
        currentFilter = filter
        filteredItems = when (filter) {
            "Pending" -> items.filter { it.status.equals("Pending", ignoreCase = true) }
            "Processing" -> items.filter { it.status.equals("Processing", ignoreCase = true) }
            "To be claimed" -> items.filter { it.status.equals("To be claimed", ignoreCase = true) }
            "Claimed" -> items.filter { it.status.equals("Claimed", ignoreCase = true) }

            else -> items
        }
        notifyDataSetChanged()
    }
}