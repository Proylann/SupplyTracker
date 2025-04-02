package com.example.upang_supply_tracker.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.upang_supply_tracker.Adapters.CartAdapter
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.databinding.FragmentCartBinding
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog

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
            },
            onSizeChange = { itemId, size ->
                cartViewModel.updateSize(itemId, size)
                Snackbar.make(binding.root, "Size updated to $size", Snackbar.LENGTH_SHORT).show()
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

            if (items.isEmpty()) {
                binding.emptyCartLayout.visibility = View.VISIBLE
                binding.cartContentLayout.visibility = View.GONE
            } else {
                binding.emptyCartLayout.visibility = View.GONE
                binding.cartContentLayout.visibility = View.VISIBLE
                binding.itemCountText.text = "${items.size} item(s)"
            }
        }

        cartViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.checkoutButton.isEnabled = !isLoading
            binding.summaryButton.isEnabled = !isLoading
            binding.clearCartButton.isEnabled = !isLoading
        }

        cartViewModel.reservationStatus.observe(viewLifecycleOwner) { status ->
            if (!status.isNullOrEmpty()) {
                Snackbar.make(binding.root, status, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.checkoutButton.setOnClickListener {
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

        val dialogView = layoutInflater.inflate(R.layout.order_summary_dialog, null)
        val orderDetailsTextView = dialogView.findViewById<TextView>(R.id.orderDetailsTextView)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        val summaryBuilder = StringBuilder("Order Summary\n\n")
        items.forEachIndexed { index, item ->
            summaryBuilder.append("${index + 1}. ${item.name} - ${item.departmentName}\n")
        }
        summaryBuilder.append("\nTotal Items: ${items.size}")
        orderDetailsTextView.text = summaryBuilder.toString()

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        confirmButton.setOnClickListener {
            alertDialog.dismiss()
            cartViewModel.checkoutCart()

            cartViewModel.reservationStatus.observe(viewLifecycleOwner) { status ->
                if (status == "Reservation submitted successfully") {

                    showThankYouDialog()
                } else {
                    Snackbar.make(binding.root, status, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showThankYouDialog() {
        val dialogView = layoutInflater.inflate(R.layout.thanks_dialog, null)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}