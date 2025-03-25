package com.example.thecube.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import com.example.thecube.R
import com.example.thecube.databinding.FragmentHomeBinding
import com.example.thecube.model.Country
import com.example.thecube.remote.RetrofitInstance
import com.example.thecube.viewModel.SharedUserViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Local copy for convenience; we'll update it once fetched.
    private var countriesList: List<Country> = emptyList()

    // Shared ViewModel to cache countries data.
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()

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
        val difficultyOptions = arrayOf("Select Difficulty", "Easy", "Medium", "Hard")
        val difficultyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, difficultyOptions)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDifficulty.adapter = difficultyAdapter

        val typeOptions = arrayOf("Select Diet", "Regular", "Vegan", "Gluten-Free")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTypeDish.adapter = typeAdapter

        // Fetch the countries once and cache them in the shared ViewModel.
        lifecycleScope.launch {
            try {
                val fetchedCountries = RetrofitInstance.api.getCountries()
                // Define the allowed countries.
                val allowedCountries = setOf("China", "Argentina", "United States", "Israel", "Romania", "Germany")
                // Filter the fetched list based on allowed countries.
                countriesList = fetchedCountries.filter { country ->
                    // Compare the common name, ignoring case.
                    allowedCountries.any { it.equals(country.name.common, ignoreCase = true) }
                }
                sharedUserViewModel.countriesData.value = countriesList
                Log.d("HomeFragment", "Fetched ${countriesList.size} allowed countries.")
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching countries: ${e.message}")
            }
        }


        // Set up the VideoView with your MP4 file from res/raw.
        val videoUri = Uri.parse("android.resource://${requireContext().packageName}/${R.raw.cube_video}")
        binding.cubeVideoView.setVideoURI(videoUri)

        // When the video is clicked, start playing it.
        binding.cubeVideoView.setOnClickListener {
            binding.cubeVideoView.start()
        }

        // When the video finishes playing, update UI and navigate after a delay.
        binding.cubeVideoView.setOnCompletionListener {
            val allCountries = sharedUserViewModel.countriesData.value
            if (!allCountries.isNullOrEmpty()) {
                // Define allowed country names.
                val allowedCountries = setOf("China", "Argentina", "United States", "Israel", "Romania", "Germany")
                // Filter the list to include only allowed countries.
                val filteredCountries = allCountries.filter { country ->
                    allowedCountries.contains(country.name.common.trim())
                }
                Log.d("HomeFragment", "Allowed countries before exclusion: ${filteredCountries.map { it.name.common }}")

                // Retrieve user's signed-up country from Firestore via SharedUserViewModel.
                val userCountry = sharedUserViewModel.currentUserData.value?.country?.trim() ?: ""

                // Exclude the user's country from the random selection.
                val randomCandidates = if (userCountry.isNotEmpty()) {
                    filteredCountries.filter { !it.name.common.trim().equals(userCountry, ignoreCase = true) }
                } else {
                    filteredCountries
                }
                Log.d("HomeFragment", "Allowed countries after exclusion: ${randomCandidates.map { it.name.common }}")

                if (randomCandidates.isNotEmpty()) {
                    // Pick a random country from the candidate list.
                    val randomIndex = (randomCandidates.indices).random()
                    val selectedCountry = randomCandidates[randomIndex]
                    binding.textViewCountry.text = "Your country: ${selectedCountry.name.common}"

                    // Load the country's flag using Picasso.
                    Picasso.get()
                        .load(selectedCountry.flags.png)
                        .resize(720, 720)
                        .noFade()
                        .centerCrop()
                        .into(binding.flagImageView)

                    // Retrieve spinner selections.
                    val spinnerDifficulty = binding.spinnerDifficulty.selectedItem?.toString() ?: "Select Difficulty"
                    val spinnerTypeDish = binding.spinnerTypeDish.selectedItem?.toString() ?: "Select Diet"

                    // If user left defaults, interpret them as "Easy" and "Regular"
                    val selectedDifficulty = if (spinnerDifficulty.equals("Select Difficulty", ignoreCase = true)) "Easy" else spinnerDifficulty
                    val selectedTypeDish = if (spinnerTypeDish.equals("Select Diet", ignoreCase = true)) "Regular" else spinnerTypeDish

                    // Create a bundle with the filters.
                    val bundle = Bundle().apply {
                        putString("country", selectedCountry.name.common)
                        if (!selectedDifficulty.equals("All", ignoreCase = true)) {
                            putString("difficulty", selectedDifficulty)
                        }
                        if (!selectedTypeDish.equals("All", ignoreCase = true)) {
                            putString("typeDish", selectedTypeDish)
                        }
                    }

                    // Delay navigation slightly.
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
