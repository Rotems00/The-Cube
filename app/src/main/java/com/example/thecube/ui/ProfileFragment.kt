package com.example.thecube.ui

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.thecube.R
import com.example.thecube.databinding.FragmentProfileBinding
import com.example.thecube.model.User
import com.example.thecube.repository.UserRepository
import com.example.thecube.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    // Safe non-null access to the binding instance
    private val binding get() = _binding!!

    // Local default image resource (if no profile picture is available)
    private val profileImageRes = R.drawable.avatar_profile

    // Launchers for picking images from gallery and taking a photo from the camera
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register launcher for the gallery (picks an image URI)
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { handleImageUri(it) }
        }

        // Register launcher for the camera (returns a Bitmap preview)
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let { handleBitmap(it) }
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            binding.profileName.text = currentUser.displayName ?: "NO NAME INITIALIZED"
            binding.profileEmail.text = currentUser.email ?: "NO EMAIL INITIALIZED"
            // Load a default image (or existing profile picture URL if stored in Firestore)
            Glide.with(this@ProfileFragment)
                .load(profileImageRes)
                .override(500, 500)
                .centerCrop()
                .into(binding.profileImage)

            // Navigate to MyDishesFragment when button is clicked
            binding.buttonMyDishes.setOnClickListener {
                findNavController().navigate(R.id.myDishesFragment)
            }
        } else {
            binding.profileName.text = "ERROR LOADING PROFILE"
            binding.profileEmail.text = "ERROR LOADING PROFILE"
        }

        // When the profile image is clicked, show a dialog to choose the image source
        binding.profileImage.setOnClickListener {
            showImageSourceDialog()
        }
    }

    /**
     * Converts the provided Bitmap into a compressed JPEG, uploads it to Cloudinary,
     * then uses the returned URL to update the UI and Firestore.
     */
    private fun handleBitmap(bitmap: Bitmap) {
        CloudinaryHelper.uploadBitmap(bitmap, onSuccess = { secureUrl ->
            // Downsample and display the image with Glide
            Glide.with(this@ProfileFragment)
                .load(secureUrl)
                .override(500, 500)
                .centerCrop()
                .into(binding.profileImage)

            // Update Firestore with the new profile picture URL
            FirebaseAuth.getInstance().currentUser?.let { currentUser ->
                val userId = currentUser.uid
                // Create an updated user object (avoid storing passwords)
                val updatedUser = User(
                    id = userId,
                    name = currentUser.displayName ?: "",
                    email = currentUser.email ?: "",
                    password = "",
                    imageUrl = secureUrl ?: "",
                    country = "" // Set/update the country as needed
                )
                UserRepository().updateUser(updatedUser) { success, error ->
                    if (success) {
                        Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update user: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }, onError = { error ->
            Toast.makeText(requireContext(), "Upload failed: $error", Toast.LENGTH_SHORT).show()
        })
    }

    /**
     * Displays a dialog allowing the user to select an image from the gallery or take a new photo.
     */
    private fun showImageSourceDialog() {
        val options = arrayOf("Choose from Gallery", "Take Photo")
        AlertDialog.Builder(requireContext())
            .setTitle("Update Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> galleryLauncher.launch("image/*")
                    1 -> cameraLauncher.launch(null)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Handles the image selected from the gallery.
     * Converts the URI to a Bitmap and then processes it.
     */
    private fun handleImageUri(uri: Uri) {
        val bitmap: Bitmap? = context?.contentResolver?.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
        bitmap?.let { handleBitmap(it) } ?: run {
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
