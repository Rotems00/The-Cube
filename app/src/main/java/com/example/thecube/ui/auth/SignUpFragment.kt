package com.example.thecube.ui.auth

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.thecube.R
import com.example.thecube.databinding.FragmentSignUpBinding
import com.example.thecube.model.Country
import com.example.thecube.model.User
import com.example.thecube.remote.RetrofitInstance
import com.example.thecube.repository.UserRepository
import com.example.thecube.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    // Retain a strong reference to the Picasso Target for profile image upload.
    private var profileImageTarget: Target? = null

    private lateinit var auth: FirebaseAuth

    // Launchers for image selection
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    // This URI will hold the location for the captured image
    private var cameraImageUri: Uri? = null
    private var profileImageUrl: String? = null

    // Lists to hold countries from the API
    private var countryList: List<Country> = emptyList()
    private var countryNames: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.signUpRoot.setOnTouchListener { v, _ ->
            hideKeyboard(v)
            false
        }

        // 1) Fetch countries from API and setup spinner
        lifecycleScope.launch {
            try {
                val countriesFromApi = RetrofitInstance.api.getCountries()
                if (countriesFromApi.isNotEmpty()) {
                    countryList = countriesFromApi
                    countryNames = countryList.map { it.name.common }.sorted()
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        countryNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerCountry.adapter = adapter
                } else {
                    Toast.makeText(requireContext(), "No countries fetched", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching countries: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 2) Register gallery launcher (for picking existing images)
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { uploadProfileImage(it) }
        }

        // 3) Register camera launcher (for taking new pictures)
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val uri = cameraImageUri
                if (uri != null) {
                    uploadProfileImage(uri)
                } else {
                    Toast.makeText(requireContext(), "Error: Image URI not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Camera capture failed", Toast.LENGTH_SHORT).show()
            }
        }

        // 4) Tap on profile image → Choose gallery or camera
        binding.imageViewProfile.setOnClickListener { showImageSourceDialog() }

        // 5) Navigate to SignInFragment if user already has an account
        binding.textViewAlreadyHaveAccount.setOnClickListener {
            val action = SignUpFragmentDirections.actionSignUpFragmentToSignInFragment()
            findNavController().navigate(action)
        }

        // 6) Sign up button: Validate fields, create user, store in Firestore
        binding.buttonSignUp.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val selectedCountry = binding.spinnerCountry.selectedItem?.toString() ?: ""

            if (binding.spinnerCountry.adapter == null || binding.spinnerCountry.adapter.count == 0) {
                Toast.makeText(requireContext(), "Countries not loaded yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedCountry.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a country", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || profileImageUrl == null) {
                Toast.makeText(requireContext(), "All fields and a profile image are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 7) Create user in FirebaseAuth
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid ?: ""
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            // 8) Store newUser in Firestore
                            val newUser = User(
                                id = userId,
                                name = name,
                                email = email,
                                password = "",
                                imageUrl = profileImageUrl ?: "",
                                country = selectedCountry
                            )
                            UserRepository().createUser(newUser) { success, error ->
                                if (success) {
                                    Toast.makeText(requireContext(), "Sign up successful", Toast.LENGTH_SHORT).show()
                                    val action = SignUpFragmentDirections.actionSignUpFragmentToHomeFragment()
                                    findNavController().navigate(action)
                                } else {
                                    Toast.makeText(requireContext(), "Error saving user: $error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Sign up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Lets the user pick from gallery or take a new photo.
     */
    private fun showImageSourceDialog() {
        val options = arrayOf("Choose from Gallery", "Take a Photo")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> galleryLauncher.launch("image/*")
                    1 -> {
                        try {
                            val photoFile = createImageFile()
                            cameraImageUri = FileProvider.getUriForFile(
                                requireContext(),
                                "${requireContext().packageName}.fileprovider",
                                photoFile
                            )
                            cameraImageUri?.let { cameraLauncher.launch(it) }
                                ?: Toast.makeText(requireContext(), "Error: Image URI not created", Toast.LENGTH_SHORT).show()
                        } catch (ex: IOException) {
                            Toast.makeText(requireContext(), "Error creating image file: ${ex.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .show()
    }

    /**
     * Creates a temporary image file in the app's external files directory.
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    /**
     * Uploads the profile image (from gallery or camera) to Cloudinary,
     * then updates [profileImageUrl] and [binding.imageViewProfile].
     */
    private fun uploadProfileImage(uri: Uri) {
        binding.progressBarProfile.visibility = View.VISIBLE

        // Create a local immutable target reference.
        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                Log.d("SignUpFragment", "Picasso loaded image, starting Cloudinary upload")
                // Start a timeout fallback: If upload doesn't complete in 30 seconds, hide progress bar.
                val timeoutRunnable = Runnable {
                    if (binding.progressBarProfile.visibility == View.VISIBLE) {
                        binding.progressBarProfile.visibility = View.GONE
                        Toast.makeText(requireContext(), "Image upload timed out", Toast.LENGTH_SHORT).show()
                    }
                }
                binding.root.postDelayed(timeoutRunnable, 30000) // 30-second timeout

                CloudinaryHelper.uploadBitmap(bitmap, onSuccess = { secureUrl ->
                    binding.root.removeCallbacks(timeoutRunnable)
                    profileImageUrl = secureUrl
                    binding.progressBarProfile.visibility = View.GONE
                    Picasso.get()
                        .load(secureUrl)
                        .resize(720,720)
                        .centerCrop()
                        .noFade()
                        .into(binding.imageViewProfile)
                    Toast.makeText(requireContext(), "Profile image uploaded", Toast.LENGTH_SHORT).show()
                }, onError = { error ->
                    binding.root.removeCallbacks(timeoutRunnable)
                    binding.progressBarProfile.visibility = View.GONE
                    Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
                })
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                binding.progressBarProfile.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
            }
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) { }
        }
        // Retain this target in a property so it isn’t garbage-collected.
        profileImageTarget = target

        Picasso.get()
            .load(uri)
            .resize(720, 720)
            .centerCrop()
            .noFade()
            .into(target)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Clear the target reference when view is destroyed.
        profileImageTarget = null
    }
}
