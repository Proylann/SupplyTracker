package com.example.upang_supply_tracker.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    private val modulesList = mutableListOf<Module>()
    private val filteredModulesList = mutableListOf<Module>()
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
        setupSearch()
        fetchModules()

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        modulesAdapter = ModulesAdapter(
            requireContext(),
            filteredModulesList,
            onItemClick = { module ->
                // Handle item click (e.g., navigate to detail screen)
                Toast.makeText(requireContext(), "Selected: ${module.title}", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = modulesAdapter
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterModules(s.toString())
            }
        })

        searchButton.setOnClickListener {
            filterModules(searchEditText.text.toString())
        }
    }

    private fun filterModules(query: String) {
        filteredModulesList.clear()

        if (query.isEmpty()) {
            filteredModulesList.addAll(modulesList)
        } else {
            val searchQuery = query.lowercase()
            modulesList.forEach { module ->
                if (module.title.lowercase().contains(searchQuery) ||
                    (module.courseName ?: "").lowercase().contains(searchQuery) ||
                    (module.Name ?: "").lowercase().contains(searchQuery)) {
                    filteredModulesList.add(module)
                }
            }
        }

        modulesAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredModulesList.isEmpty()) {
            emptyStateTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun fetchModules() {
        showLoading(true)

        apiService.getModules().enqueue(object : Callback<ModuleResponse> {
            override fun onResponse(call: Call<ModuleResponse>, response: Response<ModuleResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val modules = response.body()?.modules
                    if (modules != null) {
                        modulesList.clear()
                        modulesList.addAll(modules)

                        filteredModulesList.clear()
                        filteredModulesList.addAll(modulesList)

                        modulesAdapter.notifyDataSetChanged()
                        updateEmptyState()
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch modules", Toast.LENGTH_SHORT).show()
                    updateEmptyState()
                }
            }

            override fun onFailure(call: Call<ModuleResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                updateEmptyState()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingProgressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.GONE
        } else {
            loadingProgressBar.visibility = View.GONE
        }
    }
}