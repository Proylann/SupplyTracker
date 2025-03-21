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
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.Adapters.BookRecyclerAdapter
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

    private lateinit var apiService: ApiService
    private val booksList = mutableListOf<Books>()
    private val filteredBooksList = mutableListOf<Books>()
    private lateinit var bookAdapter: BookRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_books, container, false)

        // Initialize views
        booksRecyclerView = view.findViewById(R.id.booksRecyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)

        // Setup RecyclerView
        booksRecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize API service using the existing RetrofitClient
        apiService = RetrofitClient.instance.create(ApiService::class.java)

        // Initialize adapter
        bookAdapter = BookRecyclerAdapter(requireContext(), filteredBooksList) { book ->
            // Handle book item click
            Toast.makeText(context, "Selected: ${book.BookTitle}", Toast.LENGTH_SHORT).show()
            // You can navigate to a book details fragment or show a reservation dialog here
        }

        booksRecyclerView.adapter = bookAdapter

        // Setup search functionality
        setupSearch()

        // Fetch books
        fetchBooks()

        return view
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterBooks(s.toString())
            }
        })

        searchButton.setOnClickListener {
            filterBooks(searchEditText.text.toString())
        }
    }

    private fun filterBooks(query: String) {
        filteredBooksList.clear()

        if (query.isEmpty()) {
            filteredBooksList.addAll(booksList)
        } else {
            val searchQuery = query.lowercase()
            booksList.forEach { book ->
                if (book.BookTitle.lowercase().contains(searchQuery) ||
                    book.Department.lowercase().contains(searchQuery) ||
                    book.Course.lowercase().contains(searchQuery)) {
                    filteredBooksList.add(book)
                }
            }
        }

        bookAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredBooksList.isEmpty()) {
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

                        filteredBooksList.clear()
                        filteredBooksList.addAll(booksList)

                        bookAdapter.notifyDataSetChanged()
                        updateEmptyState()
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