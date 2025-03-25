package com.example.thecube.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.thecube.R
import com.example.thecube.databinding.FragmentAddDishBinding
import com.example.thecube.repository.UserRepository
import com.example.thecube.viewModel.DishViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.example.thecube.utils.createImageFile
import java.io.File

class AddDishFragment : Fragment() {

    // List to hold Picasso Targets to avoid garbage collection.
    private val picassoTargets = mutableListOf<Target>()

    private var _binding: FragmentAddDishBinding? = null
    private val binding get() = _binding!!

    private lateinit var dishViewModel: DishViewModel
    private var uploadedImageUrl: String? = null

    // Initialize Firestore.
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private var currentPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Log.d("AddDishFragment", "Image selected from gallery: $uri")
                uploadImage(it)
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentPhotoUri?.let { uri ->
                    Log.d("AddDishFragment", "Captured image URI: $uri")
                    val target = object : Target {
                        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                            picassoTargets.remove(this)
                            uploadCapturedImage(bitmap)
                        }
                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                            picassoTargets.remove(this)
                            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                        }
                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) { }
                    }
                    picassoTargets.add(target)
                    Picasso.get()
                        .load(uri)
                        .resize(720, 720)
                        .centerCrop()
                        .into(target)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
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

        binding.addDishRoot.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard(v)
                v.clearFocus()
            }
            false
        }

        binding.imageViewDishUpload.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Select Image Source")
                .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                    when (which) {
                        0 -> {
                            val photoFile: File = createImageFile(requireContext())
                            currentPhotoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "${requireContext().packageName}.fileprovider",
                                photoFile
                            )
                            currentPhotoUri?.let { uri ->
                                cameraLauncher.launch(uri)
                            } ?: Log.e("AddDishFragment", "Error: currentPhotoUri is null before launching camera!")
                        }
                        1 -> {
                            imagePickerLauncher.launch("image/*")
                        }
                    }
                }
                .show()
        }

        // Setup the Difficulty spinner
        val difficultyOptions = resources.getStringArray(R.array.difficulty_options)
        val difficultyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, difficultyOptions)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDifficulty.adapter = difficultyAdapter

        // Setup the Dish Type spinner
        val typeOptions = resources.getStringArray(R.array.type_dish_options)
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTypeDish.adapter = typeAdapter

        binding.buttonSubmitDish.setOnClickListener {
            binding.buttonSubmitDish.isEnabled = false
            addDishToDatabase()
        }
    }

    private fun addDishToDatabase() {
        val dishName = binding.editTextDishName.text.toString().trim()
        val dishDescription = binding.editTextDishDescription.text.toString().trim()
        val ingredients = binding.editTextIngredients.text.toString().trim()
        val steps = binding.editTextSteps.text.toString().trim()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Retrieve spinner selections for difficulty and dish type
        val selectedDifficulty = binding.spinnerDifficulty.selectedItem?.toString() ?: "Medium"
        val selectedTypeDish = binding.spinnerTypeDish.selectedItem?.toString() ?: "Regular"

        if (dishName.isEmpty() || dishDescription.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            // Re-enable the button if required fields are missing
            binding.buttonSubmitDish.isEnabled = true
            return
        }
        if (uploadedImageUrl == null) {
            Toast.makeText(requireContext(), "Please upload an image", Toast.LENGTH_SHORT).show()
            // Re-enable the button if image is missing
            binding.buttonSubmitDish.isEnabled = true
            return
        }

        UserRepository().getUser(currentUserId) { user ->
            if (user != null) {
                val dishCountry = user.country
                val firestoreId = firestore.collection("dishes").document().id

                val newDish = com.example.thecube.model.Dish(
                    id = firestoreId,
                    flagImageUrl = "",
                    dishName = dishName,
                    dishDescription = dishDescription,
                    dishSteps = steps,
                    imageUrl = uploadedImageUrl!!,
                    countLikes = 0,
                    ingredients = ingredients,
                    country = dishCountry,
                    userId = currentUserId,
                    typeDish = selectedTypeDish,
                    difficulty = selectedDifficulty
                )

                // Save to Room first.
                dishViewModel.insertDish(newDish)

                // Save to Firestore with the same ID.
                firestore.collection("dishes").document(firestoreId)
                    .set(newDish)
                    .addOnSuccessListener {
                        Log.d("AddDishFragment", "Dish saved successfully in Firestore")
                        Toast.makeText(requireContext(), "Dish added successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.profileFragment)
                    }
                    .addOnFailureListener { e ->
                        Log.e("AddDishFragment", "Error saving dish to Firestore: ${e.message}")
                        Toast.makeText(requireContext(), "Error saving dish", Toast.LENGTH_SHORT).show()
                        // Re-enable the button on error so the user can try again
                        binding.buttonSubmitDish.isEnabled = true
                    }
            } else {
                Toast.makeText(requireContext(), "Failed to retrieve user profile", Toast.LENGTH_SHORT).show()
                // Re-enable the button if the user profile could not be retrieved
                binding.buttonSubmitDish.isEnabled = true
            }
        }
    }


    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun uploadImage(uri: Uri) {
        // Show the progress bar when starting the upload.
        binding.uploadProgressBar.visibility = View.VISIBLE

        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                picassoTargets.remove(this)
                uploadCapturedImage(bitmap)
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                picassoTargets.remove(this)
                binding.uploadProgressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
            }
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) { }
        }
        picassoTargets.add(target)
        Picasso.get()
            .load(uri)
            .resize(720, 720)
            .centerCrop()
            .into(target)
    }


    private fun uploadCapturedImage(bitmap: Bitmap) {
        com.example.thecube.utils.CloudinaryHelper.uploadBitmap(bitmap, onSuccess = { secureUrl ->
            uploadedImageUrl = secureUrl
            Picasso.get()
                .load(secureUrl)
                .resize(720, 720)
                .centerCrop()
                .into(binding.imageViewDishUpload)
            // Hide the "Tap to upload" overlay after successful upload.
            binding.textOverlay.visibility = View.GONE
            // Hide the progress bar as well (if showing)
            binding.uploadProgressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
        }, onError = { error ->
            Log.e("AddDishFragment", "Image upload failed: $error")
            // Hide progress bar if there's an error
            binding.uploadProgressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
