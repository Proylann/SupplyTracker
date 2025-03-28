package com.example.upang_supply_tracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.upang_supply_tracker.R
import com.example.upang_supply_tracker.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageList = ArrayList<SlideModel>().apply {
            add(SlideModel(R.drawable.cite, ScaleTypes.CENTER_INSIDE))
            add(SlideModel(R.drawable.ccje, ScaleTypes.CENTER_INSIDE))
            add(SlideModel(R.drawable.cea, ScaleTypes.CENTER_INSIDE))
            add(SlideModel(R.drawable.cma, ScaleTypes.CENTER_INSIDE))
            add(SlideModel(R.drawable.shs, ScaleTypes.CENTER_INSIDE))
            add(SlideModel(R.drawable.cas, ScaleTypes.CENTER_INSIDE))
        }

        binding.imageSlider.setImageList(imageList)

        // Add click listener to the uniformCard
        binding.uniformCard.setOnClickListener {

            findNavController().navigate(R.id.action_navigation_home_to_uniforms)
        }

        // You can add these for the other cards as well
        binding.booksCard.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_books)
        }

        binding.modulesCard.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_modules)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}