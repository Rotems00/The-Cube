package com.example.thecube.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.thecube.R
import com.example.thecube.databinding.FragmentFavouriteBinding
import com.example.thecube.remote.RetrofitInstance
import com.example.thecube.ui.FavouriteDishesAdapter
import com.example.thecube.viewModel.DishViewModel
import com.example.thecube.viewModel.SharedUserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.MultiBrowseCarouselStrategy
import com.google.android.material.carousel.CarouselSnapHelper
import kotlinx.coroutines.launch

class FavouriteFragment : Fragment() {

    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!

    // Option 1: Use lazy delegate to automatically initialize the view model
    private val dishViewModel: DishViewModel by activityViewModels()

    private lateinit var adapter: FavouriteDishesAdapter

    // Use the same SharedUserViewModel to cache countries data.
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()
    private val currentUserId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // If you prefer manual initialization, uncomment the following:
        // dishViewModel = ViewModelProvider(requireActivity()).get(DishViewModel::class.java)
        val customFont = ResourcesCompat.getFont(requireContext(), R.font.my_font)
        binding.textViewHeader.typeface = customFont
        binding.textViewFooter.typeface = customFont
        adapter = FavouriteDishesAdapter(
            onItemClick = { dish ->
                if (dish.userId == currentUserId) {
                    val action = FavouriteFragmentDirections.actionFavouriteFragmentToEditDishFragment(dish)
                    findNavController().navigate(action)
                } else {
                    val bundle = Bundle().apply { putParcelable("dish", dish) }
                    findNavController().navigate(R.id.dishDetailFragment, bundle)
                }
            },
            onLikeToggle = { updatedDish ->
                dishViewModel.updateDishLike(updatedDish)
            }
        )


        // Set up the RecyclerView with the multi-browse carousel strategy.
        val carouselLayoutManager = CarouselLayoutManager(MultiBrowseCarouselStrategy())
        binding.recyclerViewFavourites.layoutManager = carouselLayoutManager

        val snapHelper = CarouselSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewFavourites)

        binding.recyclerViewFavourites.adapter = adapter

        // Load countries using your sharedUserViewModel.
        lifecycleScope.launch {
            try {
                val countriesList = if (sharedUserViewModel.countriesData.value.isNullOrEmpty()) {
                    val fetched = RetrofitInstance.api.getCountries()
                    sharedUserViewModel.countriesData.value = fetched
                    fetched
                } else {
                    sharedUserViewModel.countriesData.value!!
                }
                Log.d("FavouriteFragment", "Using ${countriesList.size} countries from SharedUserViewModel")
                adapter.setCountries(countriesList)
            } catch (e: Exception) {
                Log.e("FavouriteFragment", "Error fetching countries: ${e.message}")
            }
        }

        // Observe favorite dishes (assuming dishViewModel.getFavouriteDishes is implemented).
        dishViewModel.getFavouriteDishes(currentUserId).observe(viewLifecycleOwner) { dishes ->
            Log.d("FavouriteFragment", "Fetched ${dishes.size} favourite dishes for user: $currentUserId")
            adapter.submitList(dishes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
