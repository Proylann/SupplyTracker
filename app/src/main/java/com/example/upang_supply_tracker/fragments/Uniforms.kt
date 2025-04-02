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
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.Adapters.UniformAdapter
import com.example.upang_supply_tracker.Services.UserManager
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
    private lateinit var departmentFilterSwitch: SwitchCompat
    private lateinit var departmentFilterLabel: TextView
    private lateinit var apiService: ApiService
    private lateinit var uniformAdapter: UniformAdapter
    private var allUniforms: List<Uniform> = emptyList()

    private var isDepartmentFilterEnabled = true // Set to true by default

    private val departmentMap = mapOf(
        "CAHS" to "6",
        "CAS" to "9",
        "CCJE" to "11",
        "CEA" to "2",
        "CELA" to "5",
        "CITE" to "1",
        "CMA" to "10"
    )

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
        departmentFilterSwitch = view.findViewById(R.id.departmentFilterSwitch)
        departmentFilterLabel = view.findViewById(R.id.departmentFilterLabel)

        // Initialize API service
        apiService = RetrofitClient.instance.create(ApiService::class.java)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        uniformAdapter = UniformAdapter(emptyList()) { uniform ->
            Toast.makeText(requireContext(), "Selected: ${uniform.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = uniformAdapter

        // Setup search functionality
        setupSearch()
        setupDepartmentFilter()

        // Fetch uniforms when fragment is created
        fetchUniforms()
    }

    private fun setupDepartmentFilter() {
        val currentUser = UserManager.getInstance().getCurrentUser()
        val userDepartmentID = currentUser?.departmentID
        val departmentName = departmentMap.entries.find { it.value == userDepartmentID }?.key ?: "your department"

        departmentFilterLabel.text = "Show only $departmentName uniforms"
        departmentFilterSwitch.isChecked = true // Toggle ON by default

        departmentFilterSwitch.setOnCheckedChangeListener { _, isChecked ->
            isDepartmentFilterEnabled = isChecked
            filterUniforms(searchEditText.text.toString().trim())

            if (isChecked) {
                Toast.makeText(context, "Showing only $departmentName uniforms", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterUniforms(s.toString().trim())
            }
        })

        searchButton.setOnClickListener {
            filterUniforms(searchEditText.text.toString().trim())
        }
    }

    private fun fetchUniforms() {
        showLoading()

        apiService.getUniforms().enqueue(object : Callback<UniformResponse> {
            override fun onResponse(call: Call<UniformResponse>, response: Response<UniformResponse>) {
                hideLoading()

                if (response.isSuccessful) {
                    val uniformResponse = response.body()
                    if (uniformResponse?.uniforms != null && uniformResponse.uniforms.isNotEmpty()) {
                        allUniforms = uniformResponse.uniforms
                        filterUniforms(searchEditText.text.toString().trim())
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

    private fun filterUniforms(query: String) {
        val currentUser = UserManager.getInstance().getCurrentUser()
        val userDepartmentID = currentUser?.departmentID

        val filteredList = allUniforms.filter { uniform ->
            val matchesSearch = query.isEmpty() ||
                    uniform.name.contains(query, ignoreCase = true) ||
                    uniform.departmentName.contains(query, ignoreCase = true)

            val uniformDeptId = departmentMap[uniform.departmentName]
            val matchesDepartment = !isDepartmentFilterEnabled || uniformDeptId == userDepartmentID

            matchesSearch && matchesDepartment
        }

        updateUniformsList(filteredList)

        if (filteredList.isEmpty()) {
            emptyStateTextView.text = when {
                isDepartmentFilterEnabled -> {
                    val departmentName = UserManager.getInstance().getCurrentUser()?.departmentID?.let { id ->
                        departmentMap.entries.find { it.value == id }?.key
                    } ?: "your department"
                    "No uniforms available for $departmentName"
                }
                query.isNotEmpty() -> "No uniforms match your search"
                else -> "No uniforms available"
            }
            showEmptyState()
        } else {
            showRecyclerView()
        }
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