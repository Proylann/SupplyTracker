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
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.adapters.UniformAdapter
import com.example.upang_supply_tracker.backend.ApiService
import com.example.upang_supply_tracker.backend.RetrofitClient
import com.example.upang_supply_tracker.models.Uniform
import com.example.upang_supply_tracker.models.UniformResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Uniforms : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var apiService: ApiService
    private lateinit var uniformAdapter: UniformAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_uniforms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.uniformsRecyclerView)
        progressBar = view.findViewById(R.id.loadingProgressBar)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)
        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)

        // Initialize API service
        apiService = RetrofitClient.instance.create(ApiService::class.java)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        uniformAdapter = UniformAdapter(emptyList()) { uniform ->
            // This is called when the "Add to Cart" button is clicked
            // The actual adding to cart is now handled in the adapter
        }
        recyclerView.adapter = uniformAdapter

        // Setup search functionality
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchUniforms(query)
            } else {
                fetchUniforms()
            }
        }

        // Fetch uniforms when fragment is created
        fetchUniforms()
    }

    private fun fetchUniforms() {
        showLoading()

        apiService.getUniforms().enqueue(object : Callback<UniformResponse> {
            override fun onResponse(call: Call<UniformResponse>, response: Response<UniformResponse>) {
                hideLoading()

                if (response.isSuccessful) {
                    val uniformResponse = response.body()
                    if (uniformResponse?.uniforms != null && uniformResponse.uniforms.isNotEmpty()) {
                        updateUniformsList(uniformResponse.uniforms)
                        showRecyclerView()
                    } else {
                        showEmptyState()
                    }
                } else {
                    showError("Failed to load uniforms. Please try again.")
                }
            }

            override fun onFailure(call: Call<UniformResponse>, t: Throwable) {
                hideLoading()
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun searchUniforms(query: String) {
        showLoading()

        apiService.searchUniforms(query).enqueue(object : Callback<UniformResponse> {
            override fun onResponse(call: Call<UniformResponse>, response: Response<UniformResponse>) {
                hideLoading()

                if (response.isSuccessful) {
                    val uniformResponse = response.body()
                    if (uniformResponse?.uniforms != null && uniformResponse.uniforms.isNotEmpty()) {
                        updateUniformsList(uniformResponse.uniforms)
                        showRecyclerView()
                    } else {
                        showEmptyState()
                    }
                } else {
                    showError("Search failed. Please try again.")
                }
            }

            override fun onFailure(call: Call<UniformResponse>, t: Throwable) {
                hideLoading()
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun updateUniformsList(uniforms: List<Uniform>) {
        uniformAdapter.updateData(uniforms)
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateTextView.visibility = View.GONE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showRecyclerView() {
        recyclerView.visibility = View.VISIBLE
        emptyStateTextView.visibility = View.GONE
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateTextView.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}