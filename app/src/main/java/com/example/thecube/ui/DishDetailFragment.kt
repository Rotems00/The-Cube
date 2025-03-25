package com.example.thecube.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.thecube.R
import com.example.thecube.databinding.FragmentDishDetailBinding
import com.example.thecube.model.Dish
import com.example.thecube.viewModel.SharedUserViewModel
import com.squareup.picasso.Picasso

class DishDetailFragment : Fragment() {

    private var _binding: FragmentDishDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentDish: Dish
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentDish = requireArguments().getParcelable("dish")!!
        Log.d("DishDetailFragment", "Displaying details for dish: ${currentDish.dishName}")

        binding.textViewDishNameDetail.text = currentDish.dishName
        binding.textViewDishDescriptionDetail.text = currentDish.dishDescription
        binding.textViewIngredientsDetail.text = "Ingredients: ${currentDish.ingredients}"
        binding.textViewDishStepsDetail.text = "Steps: ${currentDish.dishSteps}"


        // Dish main image
        Picasso.get()
            .load(currentDish.imageUrl)
            .into(binding.imageViewDishDetail)

        // Load the flag image if available
        if (currentDish.flagImageUrl.isNotEmpty()) {
            Picasso.get()
                .load(currentDish.flagImageUrl)
                .placeholder(R.drawable.placeholder_flag)
                .error(R.drawable.placeholder_flag)
                .into(binding.imageViewFlagDetail)
        } else {
            // Look up the flag using the dish's country name from the cached countries list.
            val countries = sharedUserViewModel.countriesData.value
            if (countries != null) {
                val matchingCountry = countries.find { country ->
                    country.name.common.equals(currentDish.country.trim(), ignoreCase = true)
                }
                if (matchingCountry != null) {
                    Picasso.get()
                        .load(matchingCountry.flags.png)
                        .placeholder(R.drawable.placeholder_flag)
                        .error(R.drawable.placeholder_flag)
                        .into(binding.imageViewFlagDetail)
                } else {
                    binding.imageViewFlagDetail.setImageResource(R.drawable.placeholder_flag)
                }
            } else {
                binding.imageViewFlagDetail.setImageResource(R.drawable.placeholder_flag)
            }
        }


        binding.buttonViewComments.setOnClickListener {
            val bundle = Bundle().apply { putString("dishId", currentDish.id) }
            findNavController().navigate(R.id.action_dishDetailFragment_to_commentFragment, bundle)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}