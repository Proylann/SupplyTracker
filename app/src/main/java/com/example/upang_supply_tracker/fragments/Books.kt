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
import com.example.upang_supply_tracker.Adapters.BookRecyclerAdapter
import com.example.upang_supply_tracker.Services.UserManager
import com.example.upang_supply_tracker.backend.ApiService
import com.example.upang_supply_tracker.backend.RetrofitClient
import com.example.upang_supply_tracker.dataclass.Books
import com.example.upang_supply_tracker.dataclass.BookResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Books : Fragment() {
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var emptyStateTextView: TextView
    private lateinit var departmentFilterSwitch: SwitchCompat
    private lateinit var departmentFilterLabel: TextView

    private lateinit var apiService: ApiService
    private val booksList = mutableListOf<Books>()
    private val filteredBooksList = mutableListOf<Books>()
    private lateinit var bookAdapter: BookRecyclerAdapter

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
        val view = inflater.inflate(R.layout.fragment_books, container, false)

        booksRecyclerView = view.findViewById(R.id.booksRecyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)
        departmentFilterSwitch = view.findViewById(R.id.departmentFilterSwitch)
        departmentFilterLabel = view.findViewById(R.id.departmentFilterLabel)

        booksRecyclerView.layoutManager = LinearLayoutManager(context)
        apiService = RetrofitClient.instance.create(ApiService::class.java)

        bookAdapter = BookRecyclerAdapter(requireContext(), filteredBooksList) { book ->
            Toast.makeText(context, "Selected: ${book.BookTitle}", Toast.LENGTH_SHORT).show()
        }
        booksRecyclerView.adapter = bookAdapter

        setupSearch()
        setupDepartmentFilter()
        fetchBooks()

        return view
    }

    private fun setupDepartmentFilter() {
        val currentUser = UserManager.getInstance().getCurrentUser()
        val userDepartmentID = currentUser?.departmentID
        val departmentName = departmentMap.entries.find { it.value == userDepartmentID }?.key ?: "your department"

        departmentFilterLabel.text = "Show only $departmentName books"
        departmentFilterSwitch.isChecked = true // Toggle ON by default

        departmentFilterSwitch.setOnCheckedChangeListener { _, isChecked ->
            isDepartmentFilterEnabled = isChecked
            applyFilters(searchEditText.text.toString())

            if (isChecked) {
                Toast.makeText(context, "Showing only $departmentName books", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilters(s.toString())
            }
        })

        searchButton.setOnClickListener {
            applyFilters(searchEditText.text.toString())
        }
    }

    private fun applyFilters(searchQuery: String) {
        filteredBooksList.clear()
        val query = searchQuery.lowercase()
        val currentUser = UserManager.getInstance().getCurrentUser()
        val userDepartmentID = currentUser?.departmentID

        booksList.forEach { book ->
            val matchesSearch = searchQuery.isEmpty() ||
                    book.BookTitle.lowercase().contains(query) ||
                    book.Department.lowercase().contains(query) ||
                    book.Course.lowercase().contains(query)

            val matchesDepartment = !isDepartmentFilterEnabled || departmentMap[book.Department] == userDepartmentID

            if (matchesSearch && matchesDepartment) {
                filteredBooksList.add(book)
            }
        }

        bookAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredBooksList.isEmpty()) {
            emptyStateTextView.text = when {
                isDepartmentFilterEnabled -> {
                    val departmentName = UserManager.getInstance().getCurrentUser()?.departmentID?.let { id ->
                        departmentMap.entries.find { it.value == id }?.key
                    } ?: "your department"
                    "No books available for $departmentName"
                }
                searchEditText.text.isNotEmpty() -> "No books match your search"
                else -> "No books available"
            }
            emptyStateTextView.visibility = View.VISIBLE
            booksRecyclerView.visibility = View.GONE
        } else {
            emptyStateTextView.visibility = View.GONE
            booksRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun fetchBooks() {
        showLoading(true)
        apiService.getBooks().enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val books = response.body()?.books
                    if (books != null) {
                        booksList.clear()
                        booksList.addAll(books)
                        applyFilters(searchEditText.text.toString())
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch books", Toast.LENGTH_SHORT).show()
                    updateEmptyState()
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                updateEmptyState()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingProgressBar.visibility = View.VISIBLE
            booksRecyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.GONE
        } else {
            loadingProgressBar.visibility = View.GONE
        }
    }
}
