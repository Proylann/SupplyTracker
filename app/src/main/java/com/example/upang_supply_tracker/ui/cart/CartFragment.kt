package com.example.upang_supply_tracker.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.upang_supply_tracker.Adapters.CartAdapter
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.databinding.FragmentCartBinding
import com.example.upang_supply_tracker.models.CartItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupFilterSpinner()
        setupObservers()
        setupListeners()

        return root
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChange = { uniformId, quantity ->
                cartViewModel.updateQuantity(uniformId, quantity)
            },
            onRemoveClick = { uniformId ->
                cartViewModel.removeFromCart(uniformId)
                Snackbar.make(binding.root, "Item removed from cart", Snackbar.LENGTH_SHORT).show()
            }
        )

        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun setupFilterSpinner() {
        val filterOptions = arrayOf("All", "Books", "Uniforms", "Modules")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filterOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.filterSpinner.adapter = spinnerAdapter
        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedFilter = filterOptions[position]
                cartAdapter.applyFilter(selectedFilter)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun setupObservers() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.updateData(items)

            // Update UI based on cart state
            if (items.isEmpty()) {
                binding.emptyCartLayout.visibility = View.VISIBLE
                binding.cartContentLayout.visibility = View.GONE
            } else {
                binding.emptyCartLayout.visibility = View.GONE
                binding.cartContentLayout.visibility = View.VISIBLE

                // Update item count
                binding.itemCountText.text = "${items.size} item(s)"
            }
        }

        // Observe loading state
        cartViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.checkoutButton.isEnabled = !isLoading
            binding.summaryButton.isEnabled = !isLoading
            binding.clearCartButton.isEnabled = !isLoading
        }

        // Observe reservation status
        cartViewModel.reservationStatus.observe(viewLifecycleOwner) { status ->
            if (!status.isNullOrEmpty()) {
                Snackbar.make(binding.root, status, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.checkoutButton.setOnClickListener {
            // Implement checkout logic
            cartViewModel.checkoutCart()
        }

        binding.clearCartButton.setOnClickListener {
            cartViewModel.clearCart()
            Snackbar.make(binding.root, "Cart cleared", Snackbar.LENGTH_SHORT).show()
        }

        binding.summaryButton.setOnClickListener {
            showOrderSummaryDialog()
        }
    }

    private fun showOrderSummaryDialog() {
        val items = cartViewModel.cartItems.value
        if (items.isNullOrEmpty()) {
            Snackbar.make(binding.root, "Your cart is empty", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Create a summary of items grouped by type
        val uniformItems = items.filter { it.itemType == "UNIFORM" }
        val bookItems = items.filter { it.itemType == "BOOK" }
        val moduleItems = items.filter { it.itemType == "MODULE" }

        // Build the summary message
        val summaryBuilder = StringBuilder()
        summaryBuilder.append("Order Summary\n\n")

        // Add a section for each item type if present
        if (uniformItems.isNotEmpty()) {
            summaryBuilder.append("UNIFORMS:\n")
            uniformItems.forEachIndexed { index, item ->
                summaryBuilder.append("${index + 1}. ${item.name} - ${item.departmentName}\n")
            }
            summaryBuilder.append("\n")
        }

        if (bookItems.isNotEmpty()) {
            summaryBuilder.append("BOOKS:\n")
            bookItems.forEachIndexed { index, item ->
                val courseInfo = if (item.courseName.isNullOrEmpty()) "" else " - ${item.courseName}"
                summaryBuilder.append("${index + 1}. ${item.name}${courseInfo}\n")
            }
            summaryBuilder.append("\n")
        }

        if (moduleItems.isNotEmpty()) {
            summaryBuilder.append("MODULES:\n")
            moduleItems.forEachIndexed { index, item ->
                val courseInfo = if (item.courseName.isNullOrEmpty()) "" else " - ${item.courseName}"
                summaryBuilder.append("${index + 1}. ${item.name}${courseInfo}\n")
            }
            summaryBuilder.append("\n")
        }

        summaryBuilder.append("Total Items: ${items.size}")

        // Show the MaterialAlertDialog
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Reservation Summary")
                .setMessage(summaryBuilder.toString())
                .setPositiveButton("Proceed to Checkout") { dialog, _ ->
                    dialog.dismiss()
                    // Only call the checkout action when the user explicitly confirms
                    cartViewModel.checkoutCart()
                }
                .setNegativeButton("Edit Order") { dialog, _ ->
                    dialog.dismiss()
                    // Do nothing, just close the dialog
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}