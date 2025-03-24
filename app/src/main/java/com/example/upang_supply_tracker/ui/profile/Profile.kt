package com.example.upang_supply_tracker.ui.profile

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.upang_supply_tracker.Adapters.ReservationItemsAdapter
import com.example.upang_supply_tracker.Adapters.ReservationsAdapter
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.Services.UserManager
import com.example.upang_supply_tracker.activities.Login
import com.example.upang_supply_tracker.databinding.FragmentProfileBinding
import com.example.upang_supply_tracker.models.Reservation
import java.text.SimpleDateFormat
import java.util.Locale

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var reservationsAdapter: ReservationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup Logout Button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog(it.context) {
                performLogout(it.context)
            }
        }

        // Initialize RecyclerView and Adapter
        setupRecyclerView()

        // Setup filter spinner
        setupFilterSpinner()

        // Observe the student data
        profileViewModel.studentData.observe(viewLifecycleOwner) { student ->
            // Update UI with student data
            binding.txtStudentName.text = student.fullName
            binding.txtStudentNumber.text = "Student #: ${student.studentNumber}"
        }

        // Observe loading state
        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe errors
        profileViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }

        // Observe reservations data
        profileViewModel.reservations.observe(viewLifecycleOwner) { reservations ->
            updateReservationsList(reservations)
        }

        // Fetch student data
        profileViewModel.fetchStudentData()

        return root
    }

    private fun setupRecyclerView() {
        reservationsAdapter = ReservationsAdapter(requireContext()) { reservation ->
            // Handle reservation item click - navigate to details view
            openReservationDetails(reservation)
        }

        binding.recyclerViewReservations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reservationsAdapter
        }
    }

    private fun setupFilterSpinner() {
        // Create filter options
        val filterOptions = listOf("All", "Pending", "Processing", "To be claimed", "Claimed")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filterOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerFilter.adapter = spinnerAdapter
        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val filter = filterOptions[position]
                reservationsAdapter.applyFilter(filter)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun updateReservationsList(reservations: List<Reservation>) {
        if (reservations.isEmpty()) {
            binding.textNoReservations.visibility = View.VISIBLE
            binding.recyclerViewReservations.visibility = View.GONE
        } else {
            binding.textNoReservations.visibility = View.GONE
            binding.recyclerViewReservations.visibility = View.VISIBLE
            reservationsAdapter.updateData(reservations)
        }
    }

    private fun openReservationDetails(reservation: Reservation) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.reservation_details)

        // Set window width to 90% of screen width
        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Add direct image display for the first item (for testing)
        if (reservation.items.isNotEmpty() && !reservation.items[0].img.isNullOrEmpty()) {
            try {
                val firstItem = reservation.items[0]
                val testImageView =
                    dialog.findViewById<ImageView>(R.id.bookImage) // Add this ImageView to your dialog layout

                // Try Base64 decode
                val imageBytes = Base64.decode(firstItem.img, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (decodedImage != null) {
                    testImageView.setImageBitmap(decodedImage)
                    testImageView.visibility = View.VISIBLE
                    Log.d("ProfileFragment", "Successfully displayed test image")
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error displaying test image: ${e.message}")
            }
        }

        val txtReservationId: TextView = dialog.findViewById(R.id.txt_reservation_id)
        val txtStatus: TextView = dialog.findViewById(R.id.txt_status)
        val txtDate: TextView = dialog.findViewById(R.id.txt_date)
        val recyclerViewItems: RecyclerView = dialog.findViewById(R.id.recycler_view_items)
        val btnClose: Button = dialog.findViewById(R.id.btn_close)

        // Set reservation details
        txtReservationId.text = "Reservation #${reservation.id}"
        txtStatus.text = "Status: ${reservation.status}"

        // Format the date
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(reservation.date)
            txtDate.text = "Date: ${date?.let { outputFormat.format(it) } ?: reservation.date}"
        } catch (e: Exception) {
            txtDate.text = "Date: ${reservation.date}"
        }

        // Setup recycler view for reservation items
        recyclerViewItems.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewItems.adapter = ReservationItemsAdapter(requireContext(), reservation.items)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    fun showLogoutConfirmationDialog(context: Context, onConfirm: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout(context: Context) {
        val sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val cartItems = sharedPreferences.getString("cart_items", null) // Preserve cart

        // Clear user session using UserManager
        UserManager.getInstance().logout()

        // Restore cart items after clearing session
        if (cartItems != null) {
            sharedPreferences.edit()
                .putString("cart_items", cartItems)
                .apply()
        }

        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to login screen
        val intent = Intent(context, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}