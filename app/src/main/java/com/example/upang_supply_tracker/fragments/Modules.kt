package com.example.upang_supply_tracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upang_supply_tracker.Adapters.ModulesAdapter
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.backend.ApiService
import com.example.upang_supply_tracker.backend.RetrofitClient
import com.example.upang_supply_tracker.models.Module
import com.example.upang_supply_tracker.models.ModuleResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Modules : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var modulesAdapter: ModulesAdapter
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var emptyStateTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton

    private var modulesList: List<Module> = emptyList()
    private val apiService = RetrofitClient.instance.create(ApiService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_modules, container, false)

        // Initialize UI components
        recyclerView = view.findViewById(R.id.modulesRecyclerView)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)
        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)

        setupRecyclerView()
        setupSearchButton()
        fetchModules()

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        modulesAdapter = ModulesAdapter(
            requireContext(),
            emptyList(),
            onItemClick = { module ->
                // Handle item click (e.g., navigate to detail screen)
                Toast.makeText(requireContext(), "Selected: ${module.title}", Toast.LENGTH_SHORT).show()
            },
            onReserveClick = { module ->
                // Handle reserve button click
                Toast.makeText(requireContext(), "Reserved: ${module.title}", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = modulesAdapter
    }

    private fun setupSearchButton() {
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchModules(query)
            } else {
                modulesAdapter.updateData(modulesList)
            }
        }
    }

    private fun fetchModules() {
        showLoading(true)

        apiService.getModules().enqueue(object : Callback<ModuleResponse> {
            override fun onResponse(call: Call<ModuleResponse>, response: Response<ModuleResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val moduleResponse = response.body()
                    moduleResponse?.modules?.let { modules ->
                        if (modules.isNotEmpty()) {
                            modulesList = modules
                            modulesAdapter.updateData(modules)
                            showEmptyState(false)
                        } else {
                            showEmptyState(true)
                        }
                    } ?: run {
                        showEmptyState(true)
                        Toast.makeText(requireContext(), moduleResponse?.error ?: "No modules found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showEmptyState(true)
                    Toast.makeText(requireContext(), "Failed to load modules", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ModuleResponse>, t: Throwable) {
                showLoading(false)
                showEmptyState(true)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchModules(query: String) {
        showLoading(true)

        apiService.searchModules(query).enqueue(object : Callback<ModuleResponse> {
            override fun onResponse(call: Call<ModuleResponse>, response: Response<ModuleResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val moduleResponse = response.body()
                    moduleResponse?.modules?.let { modules ->
                        if (modules.isNotEmpty()) {
                            modulesAdapter.updateData(modules)
                            showEmptyState(false)
                        } else {
                            showEmptyState(true)
                        }
                    } ?: run {
                        showEmptyState(true)
                    }
                } else {
                    showEmptyState(true)
                    Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ModuleResponse>, t: Throwable) {
                showLoading(false)
                showEmptyState(true)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            recyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        emptyStateTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}