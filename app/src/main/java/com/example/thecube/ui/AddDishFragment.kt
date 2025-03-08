package com.example.thecube.ui

import com.example.thecube.model.Dish
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dishViewModel = ViewModelProvider(this).get(DishViewModel::class.java)

        // Hide the keyboard when tapping outside input fields.
        binding.addDishRoot.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard(v)
                v.clearFocus()
            }
            false
        }

        // Set click listener for image view to pick an image
        binding.imageViewDishUpload.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Select Image Source")
                .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                    when (which) {
                        0 -> {
                            Log.d("AddDishFragment", "Camera option selected")
                            val photoFile: File = createImageFile(requireContext())
                            currentPhotoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "${requireContext().packageName}.fileprovider",
                                photoFile
                            )
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

            Log.d("AddDishFragment", "Fetching user profile for userId: $currentUserId")
            UserRepository().getUser(currentUserId) { user ->
                if (user != null) {
                    val dishCountry = user.country
                    Log.d("AddDishFragment", "Retrieved user country: '$dishCountry'")
                    val newDish = Dish(
                        id = UUID.randomUUID().toString(),
                        flagImageUrl = "",
                        dishName = dishName,
                        dishDescription = dishDescription,
                        dishSteps = steps,
                        imageUrl = uploadedImageUrl!!,
                        countLikes = 0,
                        ingredients = ingredients,
                        country = dishCountry,
                        userId = currentUserId
                    )
                    Log.d("AddDishFragment", "New dish created with country: '${newDish.country}'")
                    dishViewModel.insertDish(newDish)
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

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

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

    private fun uploadCapturedImage(bitmap: Bitmap) {
        Log.d("AddDishFragment", "Uploading captured image")
        com.example.thecube.utils.CloudinaryHelper.uploadBitmap(bitmap, onSuccess = { secureUrl ->
            Log.d("AddDishFragment", "Image uploaded successfully, URL: $secureUrl")
            uploadedImageUrl = secureUrl
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
