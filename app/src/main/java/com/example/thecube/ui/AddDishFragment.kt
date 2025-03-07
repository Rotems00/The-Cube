package com.example.thecube.ui

import com.example.thecube.model.Dish
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.thecube.R
import com.example.thecube.databinding.FragmentAddDishBinding
import com.example.thecube.repository.UserRepository
import com.example.thecube.viewModel.DishViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.util.UUID
import com.example.thecube.utils.createImageFile



class AddDishFragment : Fragment() {

    private var _binding: FragmentAddDishBinding? = null
    private val binding get() = _binding!!

    private lateinit var dishViewModel: DishViewModel
    private var uploadedImageUrl: String? = null

    // Launcher for image selection from the gallery
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    // Launcher for taking a full-resolution picture using TakePicture()
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    // Store the URI for the photo file
    private var currentPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register gallery launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Log.d("AddDishFragment", "Image selected from gallery: $uri")
                uploadImage(it)
            }
        }
        // Register camera launcher with TakePicture contract for full-resolution image
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentPhotoUri?.let { uri ->
                    Log.d("AddDishFragment", "Captured image URI: $uri")
                    // Load full-resolution image from the file URI using Glide
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(uri)
                        .override(720, 720)
                        .centerCrop()
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                uploadCapturedImage(resource)
                            }
                            override fun onLoadCleared(placeholder: Drawable?) { }
                        })
                }
            } else {
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
                Log.w("AddDishFragment", "Camera capture failed")
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
            AlertDialog.Builder(requireContext())
                .setTitle("Select Image Source")
                .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                    when (which) {
                        0 -> {
                            Log.d("AddDishFragment", "Camera option selected")
                            // Create a temporary file for high-resolution photo
                            val photoFile: File = createImageFile(requireContext())
                            currentPhotoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "${requireContext().packageName}.fileprovider",
                                photoFile
                            )
                            // Launch camera with the obtained URI
                            currentPhotoUri?.let { uri ->
                                cameraLauncher.launch(uri)
                            } ?: Log.e("AddDishFragment", "Failed to obtain a valid photo URI")
                        }
                        1 -> {
                            Log.d("AddDishFragment", "Gallery option selected")
                            imagePickerLauncher.launch("image/*")
                        }
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
                Log.w("AddDishFragment", "Missing one or more required fields")
                return@setOnClickListener
            }
            if (uploadedImageUrl == null) {
                Toast.makeText(requireContext(), "Please upload an image", Toast.LENGTH_SHORT).show()
                Log.w("AddDishFragment", "No image uploaded")
                return@setOnClickListener
            }

            // Fetch user profile from Firestore to retrieve the user's country
            Log.d("AddDishFragment", "Fetching user profile for userId: $currentUserId")
            UserRepository().getUser(currentUserId) { user ->
                if (user != null) {
                    val dishCountry = user.country  // Use the country from the user's profile
                    Log.d("AddDishFragment", "Retrieved user country: '$dishCountry'")
                    val newDish = Dish(
                        id = UUID.randomUUID().toString(),
                        flagImageUrl = "", // Optional; update later if needed
                        dishName = dishName,
                        dishDescription = dishDescription,
                        dishSteps = steps,
                        imageUrl = uploadedImageUrl!!,  // Secure URL from Cloudinary
                        countLikes = 0,
                        ingredients = ingredients,
                        country = dishCountry,  // Set dish country from user's profile
                        userId = currentUserId
                    )
                    Log.d("AddDishFragment", "New dish created with country: '${newDish.country}'")

                    // Insert dish into Room for offline access
                    dishViewModel.insertDish(newDish)

                    // OPTIONAL: Save dish into Firestore for online use.
                    // If you haven't implemented a createDish() method in your repository, do so.
                    // For example:
                    // UserRepository().createDish(newDish) { success, error ->
                    //    if (success) {
                    //         Toast.makeText(requireContext(), "Dish added successfully", Toast.LENGTH_SHORT).show()
                    //         findNavController().navigate(R.id.myDishesFragment)
                    //         clearFields()
                    //    } else {
                    //         Toast.makeText(requireContext(), "Error saving dish: $error", Toast.LENGTH_SHORT).show()
                    //    }
                    // }

                    // For now, we assume Room storage is sufficient:
                    Toast.makeText(requireContext(), "Dish added successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.myDishesFragment)
                    clearFields()
                } else {
                    Toast.makeText(requireContext(), "Failed to retrieve user profile", Toast.LENGTH_SHORT).show()
                    Log.e("AddDishFragment", "User profile retrieval failed for userId: $currentUserId")
                }
            }
        }
    }

    // Upload an image selected from gallery
    private fun uploadImage(uri: Uri) {
        Log.d("AddDishFragment", "Uploading image from URI: $uri")
        Glide.with(requireContext())
            .asBitmap()
            .load(uri)
            .override(720, 720)
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Log.d("AddDishFragment", "Image loaded from URI, now uploading")
                    uploadCapturedImage(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) { }
            })
    }

    // Upload the captured image bitmap to Cloudinary
    private fun uploadCapturedImage(bitmap: Bitmap) {
        Log.d("AddDishFragment", "Uploading captured image")
        com.example.thecube.utils.CloudinaryHelper.uploadBitmap(bitmap, onSuccess = { secureUrl ->
            Log.d("AddDishFragment", "Image uploaded successfully, URL: $secureUrl")
            uploadedImageUrl = secureUrl
            // Display the image in the ImageView after uploading
            Glide.with(requireContext())
                .load(secureUrl)
                .override(720, 720)
                .centerCrop()
                .into(binding.imageViewDishUpload)
            Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
        }, onError = { error ->
            Log.e("AddDishFragment", "Image upload failed: $error")
            Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
        })
    }

    private fun clearFields() {
        binding.editTextDishName.text?.clear()
        binding.editTextDishDescription.text?.clear()
        binding.editTextIngredients.text?.clear()
        binding.editTextSteps.text?.clear()
        // Optionally reset the image view to a placeholder
        Glide.with(requireContext())
            .load(R.drawable.person_icon)
            .override(720, 720)
            .centerCrop()
            .into(binding.imageViewDishUpload)
        uploadedImageUrl = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
