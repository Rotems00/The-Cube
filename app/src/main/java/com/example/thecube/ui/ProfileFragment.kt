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
import com.bumptech.glide.Glide
import com.example.thecube.R
import com.example.thecube.databinding.FragmentProfileBinding
import com.example.thecube.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileImageRes = R.drawable.avatar_profile

    // Launcher for gallery selection
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    // Launcher for taking a photo (returns a Bitmap preview)
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register gallery launcher
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { handleImageUri(it) }
        }

        // Register camera launcher (TakePicturePreview returns a Bitmap)
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
            Glide.with(this)
                .load(profileImageRes)
                .override(500, 500)
                .centerCrop()
                .into(binding.profileImage)
        } else {
            binding.profileName.text = "ERROR LOADING PROFILE"
            binding.profileEmail.text = "ERROR LOADING PROFILE"
        }

        // When the profile image is clicked, show options to choose source
        binding.profileImage.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Choose from Gallery", "Take Photo")
        AlertDialog.Builder(requireContext())
            .setTitle("Update Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> galleryLauncher.launch("image/*")
                    1 -> cameraLauncher.launch(null)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Handle image selected from gallery
    private fun handleImageUri(uri: Uri) {
        // Convert the URI to a Bitmap (adjust if needed for full resolution)
        val bitmap: Bitmap? = context?.contentResolver?.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
        bitmap?.let { handleBitmap(it) } ?: run {
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    // Upload the Bitmap and update UI
    private fun handleBitmap(bitmap: Bitmap) {
        CloudinaryHelper.uploadBitmap(bitmap, onSuccess = { secureUrl ->
            Glide.with(this)
                .load(secureUrl)
                .override(500, 500)
                .centerCrop()
                .into(binding.profileImage)
            Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
        }, onError = { error ->
            Toast.makeText(requireContext(), "Upload failed: $error", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
