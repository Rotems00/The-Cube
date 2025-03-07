package com.example.thecube.ui

import com.example.thecube.model.Dish

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thecube.databinding.FragmentDishCarouselBinding
import com.example.thecube.model.Country
import com.example.thecube.remote.RetrofitInstance

import com.example.thecube.repository.LocalRecipeRepository
import com.example.thecube.viewModel.DishViewModel
import kotlinx.coroutines.launch

class DishCarouselFragment : Fragment() {

    private var _binding: FragmentDishCarouselBinding? = null
    private val binding get() = _binding!!

    private lateinit var dishAdapter: DishAdapter
    private lateinit var dishViewModel: DishViewModel

    // This variable will store the selected country's name
    private var selectedCountry: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishCarouselBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Retrieve the passed country name from the bundle
        selectedCountry = arguments?.getString("country")
        Log.d("DishCarouselFragment", "Selected country: $selectedCountry")

        // Initialize your adapter and set up RecyclerView
        dishAdapter = DishAdapter()
        binding.recyclerViewDishes.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewDishes.adapter = dishAdapter

        // Fetch the list of countries from the API and pass it to the adapter
        lifecycleScope.launch {
            try {
                val countriesList: List<Country> = RetrofitInstance.api.getCountries()

                dishAdapter.setCountries(countriesList)
            } catch (e: Exception) {
                Log.e("DishCarouselFragment", "Error fetching countries: ${e.message}")
            }
        }


        // Load the local dishes for the selected country.
        loadDishesForCountry()
    }


    private fun loadDishesForCountry() {
        if (selectedCountry != null) {
            // Filter the local dishes for the selected country
            val localDishes: List<Dish> = LocalRecipeRepository.getDishesByCountry(selectedCountry!!)
            Log.d("DishCarouselFragment", "Found ${localDishes.size} dishes for $selectedCountry")
            dishAdapter.submitList(localDishes)
        } else {
            Log.e("DishCarouselFragment", "No country provided!")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
