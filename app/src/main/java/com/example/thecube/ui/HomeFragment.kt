package com.example.thecube.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.thecube.R
import com.example.thecube.databinding.FragmentHomeBinding
import com.example.thecube.local.AppDatabase
import com.example.thecube.model.Country
import com.example.thecube.repository.LocalRecipeRepository
import com.example.thecube.remote.RetrofitInstance
import com.example.thecube.repository.DishRepository
import kotlinx.coroutines.launch
import kotlin.random.Random

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    var isAnimating = false
    private var cubeAnimator: ObjectAnimator? = null

    // List of famous countries fetched from the Countries API
    private var countriesList: List<Country> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchCountries()

        // When the cube image is tapped, call regenerateCube()
        binding.cubeImageView.setOnClickListener {
            regenerateCube()
        }
    }

    private fun fetchCountries() {
        lifecycleScope.launch {
            try {
                val allCountries = RetrofitInstance.api.getCountries()
                val famousCountriesSet = setOf(
                    "United States"
                )
                countriesList = allCountries.filter { it.name.common in famousCountriesSet }
                Log.d("HomeFragment", "Fetched ${countriesList.size} famous countries")
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching countries", e)
            }
        }
    }

    fun regenerateCube() {
        if (isAnimating) return  // ignore new requests while animating

        isAnimating = true
        val extraSpins = 5
        val randomAngle = Random.nextFloat() * 360
        val totalRotation = extraSpins * 360 + randomAngle

        cubeAnimator = ObjectAnimator.ofFloat(binding.cubeImageView, "rotationY", 0f, totalRotation).apply {
            duration = 3000
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) { }
                override fun onAnimationCancel(animation: Animator) {
                    isAnimating = false
                }
                override fun onAnimationRepeat(animation: Animator) { }
                override fun onAnimationEnd(animation: Animator) {
                    if (_binding != null) {
                        if (countriesList.isNotEmpty()) {
                            val selectedCountry = countriesList.random()
                            binding.textViewCountry.text = "Your country: ${selectedCountry.name.common}"
                            Glide.with(this@HomeFragment)
                                .load(selectedCountry.flags.png)
                                .into(binding.flagImageView)

                            // Use DishRepository to fetch dishes from Firestore (global data)
                            val dishDao = AppDatabase.getDatabase(requireContext()).dishDao()
                            val dishRepository = DishRepository(dishDao)
                            dishRepository.getDishesByCountry(selectedCountry.name.common) { dishes ->
                                Log.d("HomeFragment", "Found ${dishes.size} dishes for ${selectedCountry.name.common}")
                                if (dishes.isEmpty()) {
                                    binding.textViewCountry.append("\nNo meals found for this country.")
                                }
                                // Otherwise, do nothing – the global feed will show the dishes later.
                            }

                            binding.root.postDelayed({
                                val bundle = Bundle().apply {
                                    putString("country", selectedCountry.name.common)
                                }
                                findNavController().navigate(R.id.action_global_dishCarouselFragment, bundle)
                            }, 2000)
                        } else {
                            binding.textViewCountry.text = "No country data available"
                        }
                    }
                    isAnimating = false
                }


            })
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cubeAnimator?.cancel()  // cancel any running animation
        _binding = null
    }
}
