package com.example.thecube.ui

import com.example.thecube.model.Dish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thecube.databinding.FragmentFavouriteBinding

import com.example.thecube.ui.FavouriteDishesAdapter

class FavouriteFragment : Fragment() {

    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!

    // Dummy list of favourite dishes (with userId added)
    private val favouriteDishes = listOf(
        Dish(
            id = "1",
            flagImageUrl = "https://example.com/flag1.png",
            dishName = "Pizza",
            dishDescription = "Delicious cheese pizza",
            dishSteps = "Bake for 20 minutes",
            imageUrl = "https://example.com/dish1.jpg",
            countLikes = 150,
            ingredients = "Cheese, Dough",
            country = "Italy",
            userId = "testUser"
        ),
        Dish(
            id = "2",
            flagImageUrl = "https://example.com/flag2.png",
            dishName = "Burger",
            dishDescription = "Juicy beef burger",
            dishSteps = "Grill the patty for 5 minutes per side",
            imageUrl = "https://example.com/dish2.jpg",
            countLikes = 120,
            ingredients = "Beef, Bun, Lettuce",
            country = "USA",
            userId = "testUser"
        )
    )



    // Assume you have created a FavouriteDishesAdapter that accepts a click listener if needed
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
        // Use a horizontal LinearLayoutManager to display dishes in a carousel style
        binding.recyclerViewFavourites.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewFavourites.adapter = adapter

        // Submit the dummy list to the adapter
        adapter.submitList(favouriteDishes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
