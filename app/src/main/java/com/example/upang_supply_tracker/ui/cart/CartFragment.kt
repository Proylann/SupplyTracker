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

        // Removed loadDummyData() since we're now using real data from CartService

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
    }

    private fun setupListeners() {
        binding.checkoutButton.setOnClickListener {
            // Implement checkout logic
            cartViewModel.checkoutCart()
            Snackbar.make(binding.root, "Reservation request submitted!", Snackbar.LENGTH_LONG).show()
        }

        binding.clearCartButton.setOnClickListener {
            cartViewModel.clearCart()
            Snackbar.make(binding.root, "Cart cleared", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}