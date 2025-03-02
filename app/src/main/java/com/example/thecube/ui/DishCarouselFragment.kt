package com.example.thecube.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.thecube.R
import com.example.thecube.databinding.FragmentDishCarouselBinding
import com.example.thecube.viewModel.DishViewModel
import com.example.thecube.viewModel.RecipeViewModel

class DishCarouselFragment:Fragment(){
    private var _binding: FragmentDishCarouselBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var dishAdapter: DishAdapter
    private lateinit var dishViewModel: DishViewModel
    private lateinit var viewPager: ViewPager2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishCarouselBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recipeViewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)

        // Initialize the adapter (ListAdapter based)
        dishAdapter = DishAdapter()
        binding.viewPagerDishCarousel.adapter = dishAdapter

        // Observe the list of dishes from the remote API
        recipeViewModel.dishes.observe(viewLifecycleOwner) { dishList ->
            dishAdapter.submitList(dishList)
        }

        // Load multiple random dishes into the carousel
        recipeViewModel.loadRandomMeals(count = 5)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}