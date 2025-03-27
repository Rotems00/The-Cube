package com.example.thecube.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.thecube.R
import com.example.thecube.databinding.FragmentDishCarouselBinding
import com.example.thecube.local.AppDatabase
import com.example.thecube.repository.DishRepository
import com.example.thecube.remote.RetrofitInstance
import com.example.thecube.viewModel.SharedUserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.MultiBrowseCarouselStrategy
import com.google.android.material.carousel.CarouselSnapHelper
import kotlinx.coroutines.launch

class DishCarouselFragment : Fragment() {

    private var _binding: FragmentDishCarouselBinding? = null
    private val binding get() = _binding!!

    private lateinit var dishAdapter: DishAdapter
    private lateinit var dishRepository: DishRepository

    private var selectedCountry: String? = null
    private var selectedDifficulty: String? = null
    private var selectedTypeDish: String? = null

    // Use the same shared ViewModel to cache countries.
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishCarouselBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the selected country from arguments.
        selectedCountry = arguments?.getString("country")
        // Also retrieve optional filters.
        selectedDifficulty = arguments?.getString("difficulty")
        selectedTypeDish = arguments?.getString("typeDish")

        Log.d("DishCarouselFragment", "Selected country: $selectedCountry, difficulty: $selectedDifficulty, typeDish: $selectedTypeDish")

        // Initialize the DishRepository.
        val dishDao = AppDatabase.getDatabase(requireContext()).dishDao()
        dishRepository = DishRepository(dishDao)

        // Set up the DishAdapter with click callbacks.
        dishAdapter = DishAdapter(
            onItemClick = { dish ->
                val bundle = Bundle().apply { putParcelable("dish", dish) }
                view.findNavController().navigate(R.id.dishDetailFragment, bundle)
            },
            onLikeClicked = { updatedDish ->
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null) {
                    lifecycleScope.launch {
                        dishRepository.updateDishLike(updatedDish)
                        dishRepository.syncDishesByCountry(updatedDish.country)
                    }
                }
            }
        )

        // Set up the RecyclerView with the multi-browse carousel strategy.
        val carouselLayoutManager = CarouselLayoutManager(MultiBrowseCarouselStrategy())
        binding.recyclerViewDishes.layoutManager = carouselLayoutManager

        val snapHelper = CarouselSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewDishes)

        binding.recyclerViewDishes.adapter = dishAdapter

        // Use sharedUserViewModel to load countries.
        lifecycleScope.launch {
            try {
                val countriesList = if (sharedUserViewModel.countriesData.value.isNullOrEmpty()) {
                    val fetched = RetrofitInstance.api.getCountries()
                    sharedUserViewModel.countriesData.value = fetched
                    fetched
                } else {
                    sharedUserViewModel.countriesData.value!!
                }
                Log.d("DishCarouselFragment", "Using ${countriesList.size} countries from SharedUserViewModel")
                dishAdapter.setCountries(countriesList)

                // Optionally, sync dishes for the selected country.
                selectedCountry?.let { country ->
                    dishRepository.syncDishesByCountry(country)
                }
            } catch (e: Exception) {
                Log.e("DishCarouselFragment", "Error fetching countries or syncing dishes: ${e.message}")
            }
        }

        // Observe Room LiveData for dishes based on filters.
        selectedCountry?.let { country ->
            if (!selectedDifficulty.isNullOrEmpty() && !selectedTypeDish.isNullOrEmpty() &&
                !selectedDifficulty.equals("All", ignoreCase = true) &&
                !selectedTypeDish.equals("All", ignoreCase = true)
            ) {
                dishRepository.getDishesByCountryAndFilters(country, selectedDifficulty!!, selectedTypeDish!!)
                    .observe(viewLifecycleOwner) { dishes ->
                        Log.d("DishCarouselFragment", "Filtered LiveData: Found ${dishes.size} dishes")
                        updateUIForDishes(dishes)
                    }
            } else {
                dishRepository.getDishesByCountryFromRoom(country)
                    .observe(viewLifecycleOwner) { dishes ->
                        Log.d("DishCarouselFragment", "LiveData update: Found ${dishes.size} dishes")
                        updateUIForDishes(dishes)
                    }
            }
        } ?: Log.e("DishCarouselFragment", "No country provided!")
    }

    // Helper function to update UI based on the list of dishes.
    private fun updateUIForDishes(dishes: List<com.example.thecube.model.Dish>) {
        if (dishes.isEmpty()) {
            binding.recyclerViewDishes.visibility = View.GONE
            binding.emptyAnimationView.visibility = View.VISIBLE
            binding.emptyAnimationView.playAnimation()
        } else {
            binding.emptyAnimationView.cancelAnimation()
            binding.emptyAnimationView.visibility = View.GONE
            binding.recyclerViewDishes.visibility = View.VISIBLE
            dishAdapter.submitList(dishes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
