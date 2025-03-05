package com.example.thecube.ui
import androidx.navigation.fragment.findNavController
import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.thecube.R
import com.example.thecube.databinding.FragmentHomeBinding
import com.example.thecube.model.Country
import com.example.thecube.repository.LocalRecipeRepository
import com.example.thecube.remote.RetrofitInstance
import kotlinx.coroutines.launch
import kotlin.random.Random

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        // Fetch the list of famous countries from the Countries API
        fetchCountries()

        binding.cubeImageView.setOnClickListener {
            // Animate the cube: several full spins + a random additional rotation
            val extraSpins = 5
            val randomAngle = Random.nextFloat() * 360
            val totalRotation = extraSpins * 360 + randomAngle

            val animator = ObjectAnimator.ofFloat(binding.cubeImageView, "rotationY", 0f, totalRotation)
            animator.duration = 3000
            animator.start()

            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    if (countriesList.isNotEmpty()) {
                        // Select a random famous country
                        val selectedCountry = countriesList.random()
                        binding.textViewCountry.text = "Your country: ${selectedCountry.name.common}"

                        // Load the flag image using Glide with placeholder/error (ensure URLs and drawables are valid)
                        Glide.with(this@HomeFragment)
                            .load(selectedCountry.flags.png)

                            .into(binding.flagImageView)

                        // Optionally, show a meal from local repository for demo purposes
                        val meals = LocalRecipeRepository.getDishesByCountry(selectedCountry.name.common)
                        if (meals.isEmpty()) {
                            binding.textViewCountry.append("\nNo meals found for this country.")
                        }

                        // Wait 2 seconds after displaying the country info, then navigate to the carousel screen.
                        binding.root.postDelayed({
                            val bundle = Bundle().apply {
                                putString("country", selectedCountry.name.common)
                            }
                            findNavController().navigate(R.id.action_homeFragment_to_dishCarouselFragment, bundle)
                        }, 2000)
                    }
                    else {
                        binding.textViewCountry.text = "No country data available"
                    }
                }
            })
        }

    }

    private fun fetchCountries() {
        lifecycleScope.launch {
            try {
                // Fetch all countries via your Retrofit instance.
                val allCountries = RetrofitInstance.api.getCountries()

                // Define the set of famous countries.
                val famousCountriesSet = setOf(
                    "United States", "United Kingdom", "France", "Italy", "Germany",
                    "Spain", "Japan", "China", "India", "Brazil", "Mexico",
                    "Canada", "Australia", "Russia", "South Korea"
                )

                // Filter to include only famous countries.
                countriesList = allCountries.filter { it.name.common in famousCountriesSet }
                Log.d("HomeFragment", "Fetched ${countriesList.size} famous countries")
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching countries", e)
            }
        }
    }
    fun regenerateCube() {
        // Define the number of extra spins and a random additional angle
        val extraSpins = 5
        val randomAngle = kotlin.random.Random.nextFloat() * 360
        val totalRotation = extraSpins * 360 + randomAngle

        // Animate the cubeImageView (make sure your view's ID is correct)
        val animator = ObjectAnimator.ofFloat(binding.cubeImageView, "rotationY", 0f, totalRotation)
        animator.duration = 3000  // Duration in milliseconds
        animator.start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
