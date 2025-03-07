package com.example.thecube.ui

import com.example.thecube.model.Dish

import android.app.AlertDialog
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
import com.example.thecube.databinding.FragmentAddDishBinding

import com.example.thecube.utils.CloudinaryHelper
import com.example.thecube.viewModel.DishViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class AddDishFragment : Fragment() {

    private var _binding: FragmentAddDishBinding? = null
    private val binding get() = _binding!!

    private lateinit var dishViewModel: DishViewModel
    private var uploadedImageUrl: String? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>

    // Launcher for image selection from the gallery
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadImage(it)
            }
        }
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
        _binding = FragmentAddDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dishViewModel = ViewModelProvider(this).get(DishViewModel::class.java)

        // Set click listener for image view to pick an image
        binding.imageViewDishUpload.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.imageViewDishUpload.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Select Image Source")
                .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                    when (which) {
                        0 -> cameraLauncher.launch(null)
                        1 -> imagePickerLauncher.launch("image/*")
                    }
                }
                .show()
        }


        binding.buttonSubmitDish.setOnClickListener {
            val dishName = binding.editTextDishName.text.toString().trim()
            val dishDescription = binding.editTextDishDescription.text.toString().trim()
            val ingredients = binding.editTextIngredients.text.toString().trim()
            val steps = binding.editTextSteps.text.toString().trim()
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            // Basic validation
            if (dishName.isEmpty() || dishDescription.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (uploadedImageUrl == null) {
                Toast.makeText(requireContext(), "Please upload an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create and insert the dish
            val newDish = Dish(
                id = UUID.randomUUID().toString(),
                flagImageUrl = "", // Optional: set if available
                dishName = dishName,
                dishDescription = dishDescription,
                dishSteps = steps,
                imageUrl = uploadedImageUrl!!,
                countLikes = 0,
                ingredients = ingredients,
                country = "", // Set if applicable
                userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            )
            dishViewModel.insertDish(newDish)
            Toast.makeText(requireContext(), "Dish added successfully", Toast.LENGTH_SHORT).show()

            // Navigate to MyDishesFragment after adding a dish
            findNavController().navigate(R.id.myDishesFragment)

            clearFields()
        }

    }

    // Use Glide to load a downsampled Bitmap and then upload it
    private fun uploadImage(uri: Uri) {
        Glide.with(requireContext())
            .asBitmap()
            .load(uri)
            .override(500, 500) // Downsample to 500x500 pixels
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    CloudinaryHelper.uploadBitmap(resource, onSuccess = { secureUrl ->
                        uploadedImageUrl = secureUrl
                        // Instead of setting the full image via setImageURI(), use Glide to display the downsampled image:
                        Glide.with(requireContext())
                            .load(secureUrl)
                            .override(500, 500)
                            .centerCrop()
                            .into(binding.imageViewDishUpload)
                        Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    }, onError = { error ->
                        Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
                    })
                }
                override fun onLoadCleared(placeholder: Drawable?) { }
            })
    }

    private fun clearFields() {
        binding.editTextDishName.text?.clear()
        binding.editTextDishDescription.text?.clear()
        binding.editTextIngredients.text?.clear()
        binding.editTextSteps.text?.clear()
        // Reload the downsampled placeholder image via Glide
        Glide.with(requireContext())
            .load(R.drawable.person_icon)
            .override(500, 500)
            .centerCrop()
            .into(binding.imageViewDishUpload)
        uploadedImageUrl = null
    }
    private fun uploadCapturedImage(bitmap: Bitmap) {
        // Display the downsampled Bitmap in the ImageView using Glide
        Glide.with(requireContext())
            .asBitmap()
            .load(bitmap)
            .override(500, 500)
            .centerCrop()
            .into(binding.imageViewDishUpload)

        // Upload the Bitmap to Cloudinary
        CloudinaryHelper.uploadBitmap(bitmap, onSuccess = { secureUrl ->
            uploadedImageUrl = secureUrl
            Toast.makeText(requireContext(), "Image captured and uploaded", Toast.LENGTH_SHORT).show()
        }, onError = { error ->
            Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
