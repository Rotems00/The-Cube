package com.example.thecube.ui

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.example.thecube.R
import com.example.thecube.databinding.FragmentHomeBinding
import com.example.thecube.model.Country
import com.example.thecube.remote.RetrofitInstance
import com.example.thecube.viewModel.SharedUserViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var countriesList: List<Country> = emptyList()
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()

    // Variables to track valid spinner selections
    private var isDifficultyValid = false
    private var isDietValid = false
    private var animationStarted = false // To ensure we only start once

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard()

        // Setup spinners
        val difficultyOptions = arrayOf("Select Difficulty", "Easy", "Medium", "Hard")
        val difficultyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, difficultyOptions)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDifficulty.adapter = difficultyAdapter

        val typeOptions = arrayOf("Select Diet", "Regular", "Vegan", "Gluten-Free")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTypeDish.adapter = typeAdapter

        // Listen for spinner selections
        binding.spinnerDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = binding.spinnerDifficulty.selectedItem.toString()
                isDifficultyValid = selected != "Select Difficulty"
                maybeStartAnimationIfNeeded()
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        binding.spinnerTypeDish.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = binding.spinnerTypeDish.selectedItem.toString()
                isDietValid = selected != "Select Diet"
                maybeStartAnimationIfNeeded()
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        // Fetch countries (same as before)
        lifecycleScope.launch {
            try {
                val fetchedCountries = RetrofitInstance.api.getCountries()
                val allowedCountries = setOf("China", "Argentina", "United States", "Israel", "Romania", "Germany")
                countriesList = fetchedCountries.filter { country ->
                    allowedCountries.any { it.equals(country.name.common, ignoreCase = true) }
                }
                sharedUserViewModel.countriesData.value = countriesList
                Log.d("HomeFragment", "Fetched ${countriesList.size} allowed countries.")
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching countries: ${e.message}")
            }
        }

        // Lottie reference
        val lottieView: LottieAnimationView = binding.cubeLottieView
        // We only want one loop, then end
        lottieView.repeatCount = 0

        // Listen for when that 1 loop ends
        lottieView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                // The single loop has ended -> do your random-country logic
                proceedAfterAnimation()
            }
        })
    }

    /**
     * Called each time a spinner selection is changed.
     * If both spinners are valid, we start the animation once.
     */
    private fun maybeStartAnimationIfNeeded() {
        // If both spinners are valid and we haven't started animation yet => Start
        if (isDifficultyValid && isDietValid && !animationStarted) {
            animationStarted = true
            binding.cubeLottieView.playAnimation()
        }
    }

    /**
     * Called once the Lottie animation (single loop) finishes.
     */
    private fun proceedAfterAnimation() {
        val allCountries = sharedUserViewModel.countriesData.value
        if (!allCountries.isNullOrEmpty()) {
            val allowedCountries = setOf("China", "Argentina", "United States", "Israel", "Romania", "Germany")
            val filteredCountries = allCountries.filter {
                allowedCountries.contains(it.name.common.trim())
            }

            Log.d("HomeFragment", "Allowed countries before exclusion: ${filteredCountries.map { it.name.common }}")

            val userCountry = sharedUserViewModel.currentUserData.value?.country?.trim() ?: ""
            val randomCandidates = if (userCountry.isNotEmpty()) {
                filteredCountries.filter {
                    !it.name.common.trim().equals(userCountry, ignoreCase = true)
                }
            } else {
                filteredCountries
            }
            Log.d("HomeFragment", "Allowed countries after exclusion: ${randomCandidates.map { it.name.common }}")

            if (randomCandidates.isNotEmpty()) {
                val randomIndex = (randomCandidates.indices).random()
                val selectedCountry = randomCandidates[randomIndex]
                binding.textViewCountry.text = "Your country: ${selectedCountry.name.common}"

                Picasso.get()
                    .load(selectedCountry.flags.png)
                    .resize(720, 720)
                    .noFade()
                    .centerCrop()
                    .into(binding.flagImageView)

                val spinnerDifficulty = binding.spinnerDifficulty.selectedItem?.toString() ?: "Easy"
                val spinnerDiet = binding.spinnerTypeDish.selectedItem?.toString() ?: "Regular"

                val bundle = Bundle().apply {
                    putString("country", selectedCountry.name.common)
                    putString("difficulty", spinnerDifficulty)
                    putString("typeDish", spinnerDiet)
                }

                // Delay navigation slightly (optional)
                binding.root.postDelayed({
                    findNavController().navigate(R.id.action_global_dishCarouselFragment, bundle)
                }, 1200)
            } else {
                binding.textViewCountry.text = "No allowed countries available"
                Log.e("HomeFragment", "Filtered countries list is empty after excluding user's country.")
            }
        } else {
            binding.textViewCountry.text = "No country data available"
            Log.e("HomeFragment", "Shared countries data is empty.")
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
