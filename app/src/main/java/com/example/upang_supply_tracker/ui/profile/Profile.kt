package com.example.upang_supply_tracker.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.upang_supply_tracker.databinding.FragmentProfileBinding

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observe the student data
        profileViewModel.studentData.observe(viewLifecycleOwner) { student ->
            // Update UI with student data
            binding.txtStudentName.text = student.fullName
            binding.txtStudentNumber.text = "Student #: ${student.studentNumber}"
        }

        // Observe loading state
        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->

        }

        // Observe errors
        profileViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }

        // Fetch student data
        profileViewModel.fetchStudentData()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}