package com.example.thecube.ui

import com.example.thecube.model.Dish

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.thecube.R
import com.example.thecube.databinding.FragmentEditDishBinding

import com.example.thecube.utils.CloudinaryHelper
import com.example.thecube.viewModel.DishViewModel

class EditDishFragment : Fragment() {

    private var _binding: FragmentEditDishBinding? = null
    private val binding get() = _binding!!

    private lateinit var dishViewModel: DishViewModel
    private lateinit var currentDish: Dish
    private var updatedImageUrl: String? = null

    // Launcher for picking a new image from gallery
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    // Launcher for taking a new photo
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Register launcher for gallery
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadImage(it)
            }
        }
        // Register launcher for camera preview (if needed)
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let {
                uploadCapturedImage(it)
            }
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentEditDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dishViewModel = ViewModelProvider(this).get(DishViewModel::class.java)

        // Assume the dish to edit is passed as an argument (via SafeArgs or Bundle)
        // For example, using a Bundle:
        currentDish = requireArguments().getParcelable("dish")!!
        updatedImageUrl = currentDish.imageUrl

        // Populate fields with current dish data
        binding.editTextDishName.setText(currentDish.dishName)
        binding.editTextDishDescription.setText(currentDish.dishDescription)
        binding.editTextIngredients.setText(currentDish.ingredients)
        binding.editTextSteps.setText(currentDish.dishSteps)

        // Load current image using Glide
        Glide.with(requireContext())
            .load(currentDish.imageUrl)
            .override(720, 720)
            .centerCrop()
            .into(binding.imageViewDishUpload)

        // Set click listener to allow image change
        binding.imageViewDishUpload.setOnClickListener {
            // Let user choose: take a new photo or choose from gallery
            // For simplicity, here's a gallery-only option:
            imagePickerLauncher.launch("image/*")
            // You can also show a dialog to choose between camera and gallery if desired.
        }

        // Set click listener for the update button
        binding.buttonUpdateDish.setOnClickListener {
            val updatedName = binding.editTextDishName.text.toString().trim()
            val updatedDescription = binding.editTextDishDescription.text.toString().trim()
            val updatedIngredients = binding.editTextIngredients.text.toString().trim()
            val updatedSteps = binding.editTextSteps.text.toString().trim()

            if (updatedName.isEmpty() || updatedDescription.isEmpty() ||
                updatedIngredients.isEmpty() || updatedSteps.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Define updatedDish as a local variable
            val updatedDish = currentDish.copy(
                dishName = updatedName,
                dishDescription = updatedDescription, // Only the updated description
                dishSteps = updatedSteps,             // And the updated steps
                ingredients = updatedIngredients,
                imageUrl = updatedImageUrl ?: currentDish.imageUrl
            )

            dishViewModel.updateDish(updatedDish)
            Toast.makeText(requireContext(), "Dish updated successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.myDishesFragment)
        }


    }

    // Use Glide to load and downsample image from gallery and then upload it
    private fun uploadImage(uri: Uri) {
        Glide.with(requireContext())
            .asBitmap()
            .load(uri)
            .override(720, 720)
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    CloudinaryHelper.uploadBitmap(resource, onSuccess = { secureUrl ->
                        updatedImageUrl = secureUrl
                        // Display the new image from the secure URL
                        Glide.with(requireContext())
                            .load(secureUrl)
                            .override(720, 720)
                            .centerCrop()
                            .into(binding.imageViewDishUpload)
                        Toast.makeText(requireContext(), "Image updated successfully", Toast.LENGTH_SHORT).show()
                    }, onError = { error ->
                        Toast.makeText(requireContext(), "Image update failed: $error", Toast.LENGTH_SHORT).show()
                    })
                }
                override fun onLoadCleared(placeholder: Drawable?) { }
            })
    }

    // If using camera capture, similar function:
    private fun uploadCapturedImage(bitmap: Bitmap) {
        Glide.with(requireContext())
            .asBitmap()
            .load(bitmap)
            .override(720, 720)
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    CloudinaryHelper.uploadBitmap(resource, onSuccess = { secureUrl ->
                        updatedImageUrl = secureUrl
                        // Display the new image from the secure URL
                        Glide.with(requireContext())
                            .load(secureUrl)
                            .override(720, 720)
                            .centerCrop()
                            .into(binding.imageViewDishUpload)
                        Toast.makeText(requireContext(), "Image updated successfully", Toast.LENGTH_SHORT).show()
                    }, onError = { error ->
                        Toast.makeText(requireContext(), "Image update failed: $error", Toast.LENGTH_SHORT).show()
                    })
                }
                override fun onLoadCleared(placeholder: Drawable?) { }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
