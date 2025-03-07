package com.example.thecube.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thecube.databinding.FragmentFavouriteBinding
import com.example.thecube.model.Dish
import com.example.thecube.model.Country
import com.example.thecube.remote.RetrofitInstance
import com.example.thecube.ui.FavouriteDishesAdapter
import kotlinx.coroutines.launch

class FavouriteFragment : Fragment() {

    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!

    // Dummy list of favourite dishes (for now)
    private val favouriteDishes = listOf(
        Dish(
            id = "1",
            flagImageUrl = "", // We'll load from API now
            dishName = "Pizza",
            dishDescription = "Delicious cheese pizza",
            dishSteps = "Bake for 20 minutes",
            imageUrl = "https://images.ctfassets.net/j8tkpy1gjhi5/5OvVmigx6VIUsyoKz1EHUs/b8173b7dcfbd6da341ce11bcebfa86ea/Salami-pizza-hero.jpg?w=1440&fm=webp&q=80",  // Replace with a valid URL if needed
            countLikes = 150,
            ingredients = "Cheese, Dough",
            country = "Italy",
            userId = "testUser"
        ),
        Dish(
            id = "2",
            flagImageUrl = "", // We'll load from API now
            dishName = "Burger",
            dishDescription = "Juicy beef burger",
            dishSteps = "Grill the patty for 5 minutes per side",
            imageUrl = "https://www.allrecipes.com/thmb/5JVfA7MxfTUPfRerQMdF-nGKsLY=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/25473-the-perfect-basic-burger-DDMFS-4x3-56eaba3833fd4a26a82755bcd0be0c54.jpg",  // Replace with a valid URL if needed
            countLikes = 120,
            ingredients = "Beef, Bun, Lettuce",
            country = "United States",
            userId = "testUser"
        )
    )

    private lateinit var adapter: FavouriteDishesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FavouriteDishesAdapter()
        binding.recyclerViewFavourites.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewFavourites.adapter = adapter

        // Fetch the list of countries from the API and set them in the adapter
        lifecycleScope.launch {
            try {
                val countriesList: List<Country> = RetrofitInstance.api.getCountries()
                Log.d("FavouriteFragment", "Fetched ${countriesList.size} countries")
                countriesList.forEach { country ->
                    Log.d("FavouriteFragment", "API country: '${country.name.common.trim()}'")
                }
                adapter.setCountries(countriesList)
            } catch (e: Exception) {
                Log.e("FavouriteFragment", "Error fetching countries: ${e.message}")
            }
        }

        // Submit the dummy list of dishes to the adapter
        favouriteDishes.forEach { dish ->
            Log.d("FavouriteFragment", "Dish: '${dish.dishName}', Country: '${dish.country.trim()}'")
        }
        adapter.submitList(favouriteDishes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
