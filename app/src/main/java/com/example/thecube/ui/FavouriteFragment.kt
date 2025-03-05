package com.example.thecube.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thecube.databinding.FragmentFavouriteBinding
import com.example.thecube.model.Dish
import com.example.thecube.ui.FavouriteDishesAdapter

class FavouriteFragment : Fragment() {

    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!

    // Dummy list of favourite dishes
    private val favouriteDishes = listOf(
        Dish("1", "https://example.com/dish1.jpg", "Pizza", "Delicious cheese pizza", "https://example.com/flag1.png", 150, "Cheese, Dough", "Italy"),
        Dish("2", "https://example.com/dish2.jpg", "Burger", "Juicy beef burger", "https://example.com/flag2.png", 120, "Beef, Bun, Lettuce", "USA")
    )

    // Assume you have created a FavouriteDishesAdapter
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
        binding.recyclerViewFavourites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavourites.adapter = adapter

// Then submit the list:
        adapter.submitList(favouriteDishes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
