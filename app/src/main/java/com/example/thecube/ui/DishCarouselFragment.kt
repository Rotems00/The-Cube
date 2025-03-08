package com.example.thecube.ui

import com.example.thecube.model.Dish
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thecube.databinding.FragmentDishCarouselBinding
import com.example.thecube.local.AppDatabase
import com.example.thecube.model.Country
import com.example.thecube.remote.RetrofitInstance
import com.example.thecube.repository.DishRepository
import com.example.thecube.ui.DishAdapter
import kotlinx.coroutines.launch

class DishCarouselFragment : Fragment() {

    private var _binding: FragmentDishCarouselBinding? = null
    private val binding get() = _binding!!

    private lateinit var dishAdapter: DishAdapter

    // This variable stores the selected country's name passed in arguments.
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

        // Retrieve the selected country name from the bundle.
        selectedCountry = arguments?.getString("country")
        Log.d("DishCarouselFragment", "Selected country: $selectedCountry")

        // Initialize the adapter and set up the RecyclerView.
        dishAdapter = DishAdapter()
        binding.recyclerViewDishes.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewDishes.adapter = dishAdapter

        // Fetch the list of countries from the API for flag images and pass them to the adapter.
        lifecycleScope.launch {
            try {
                val countriesList: List<Country> = RetrofitInstance.api.getCountries()
                Log.d("DishCarouselFragment", "Fetched ${countriesList.size} countries from API")
                dishAdapter.setCountries(countriesList)
            } catch (e: Exception) {
                Log.e("DishCarouselFragment", "Error fetching countries: ${e.message}")
            }
        }

        // Load the dishes for the selected country.
        loadDishesForCountry()
    }

    private fun loadDishesForCountry() {
        selectedCountry?.let { country ->
            Log.d("DishCarouselFragment", "Loading dishes for country: $country")
            // Get the Room DAO and create an instance of DishRepository.
            val dishDao = AppDatabase.getDatabase(requireContext()).dishDao()
            val dishRepository = DishRepository(dishDao)
            // Query Firestore for dishes matching the selected country.
            dishRepository.getDishesByCountry(country) { dishes ->
                Log.d("DishCarouselFragment", "Found ${dishes.size} dishes for $country")
                dishes.forEach { dish ->
                    Log.d("DishCarouselFragment", "Dish: ${dish.dishName}, Country: ${dish.country}")
                }
                dishAdapter.submitList(dishes)
            }
        } ?: Log.e("DishCarouselFragment", "No country provided!")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
