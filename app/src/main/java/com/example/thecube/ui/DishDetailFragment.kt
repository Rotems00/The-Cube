package com.example.thecube.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.thecube.R
import com.example.thecube.databinding.FragmentDishDetailBinding
import com.example.thecube.model.Dish

class DishDetailFragment : Fragment() {

    private var _binding: FragmentDishDetailBinding? = null
    private val binding get() = _binding!!

    // Retrieve the dish from arguments (using Safe Args or Bundle)
    private lateinit var dish: Dish

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If using Safe Args, get the dish object
        dish = requireArguments().getParcelable("dish")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDishDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Bind dish data to UI
        binding.textViewDishNameDetail.text = dish.dishName
        binding.textViewDishDescriptionDetail.text = dish.dishDescription
        binding.textViewIngredientsDetail.text = "Ingredients: ${dish.ingredients}"
        binding.textViewDishStepsDetail.text = "Steps: ${dish.dishSteps}"

        Glide.with(this)
            .load(dish.imageUrl)
            .into(binding.imageViewDishDetail)

        // Set click listener for "View Comments" button
        binding.buttonViewComments.setOnClickListener {
            // Pass the dish id to the comment fragment
            val bundle = Bundle().apply {
                putString("dishId", dish.id)
            }
            findNavController().navigate(R.id.commentFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
