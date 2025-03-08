package com.example.thecube.ui

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.thecube.R
import com.example.thecube.databinding.FragmentProfileBinding
import com.example.thecube.local.AppDatabase
import com.example.thecube.model.User
import com.example.thecube.repository.UserRepository
import com.example.thecube.utils.CloudinaryHelper
import com.example.thecube.viewModel.SharedUserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel for user data
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "ProfileFragmentDebug"

    // Launchers for picking images from gallery and taking a photo
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
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called after the view is created. We:
     * 1) Observe sharedUserViewModel to update UI
     * 2) Fetch user from Firestore (only once) and store in sharedUserViewModel
     */
    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // (1) Observe the shared user data
        sharedUserViewModel.currentUserData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Log.d(TAG, "Observed user in ViewModel: ${user.imageUrl}")
                // Update UI text
                binding.profileName.text = if (user.name.isNotEmpty()) user.name else "NO NAME"
                binding.profileEmail.text = if (user.email.isNotEmpty()) user.email else "NO EMAIL"

                // Update photo if we have a valid URL
                if (user.imageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(user.imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .override(720, 720)
                        .centerCrop()
                        .into(binding.profileImage)
                } else {
                    // If there's no image, optionally load a placeholder or do nothing
                    Glide.with(this)
                        .load(R.drawable.avatar_profile)
                        .override(720, 720)
                        .centerCrop()
                        .into(binding.profileImage)
                }
            } else {
                // If user is null, show some fallback
                binding.profileName.text = "ERROR LOADING PROFILE"
                binding.profileEmail.text = "ERROR LOADING PROFILE"
                Glide.with(this)
                    .load(R.drawable.avatar_profile)
                    .override(720, 720)
                    .centerCrop()
                    .into(binding.profileImage)
            }
        }

        // (2) Actually fetch the user from Firestore if we don't already have it
        val userId = auth.currentUser?.uid
        if (userId != null && sharedUserViewModel.currentUserData.value == null) {
            loadUserFromFirestore(userId)
        }

        // My Dishes button
        binding.buttonMyDishes.setOnClickListener {
            findNavController().navigate(R.id.myDishesFragment)
        }

        // Sign Out button
        binding.btnLogout.setOnClickListener {
            // Sign out from FirebaseAuth
            auth.signOut()

            // Clear local Room data for the user
            clearLocalData()

            // Clear the shared user data in your SharedUserViewModel
            sharedUserViewModel.currentUserData.value = null

            // Optionally, clear any cached data if needed

            // Navigate to the sign-in screen while clearing the back stack
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.signInFragment, true)
                .build()
            findNavController().navigate(R.id.signInFragment, null, navOptions)
        }


        // Tapping on profile image => dialog to choose source
        binding.profileImage.setOnClickListener {
            showImageSourceDialog()
        }
    }

    /**
     * Actually fetches the user doc from Firestore, sets it in sharedUserViewModel.
     */
    private fun loadUserFromFirestore(userId: String) {
        UserRepository().getUser(userId) { user ->
            if (user != null) {
                Log.d(TAG, "loadUserFromFirestore: user found, imageUrl=${user.imageUrl}")
                // Store user in shared ViewModel => triggers observer to update UI
                sharedUserViewModel.currentUserData.value = user
            } else {
                Log.d(TAG, "loadUserFromFirestore: user doc not found")
                // We can set null or handle an error case
                sharedUserViewModel.currentUserData.value = null
            }
        }
    }

    /**
     * Handle a newly captured or picked Bitmap, upload to Cloudinary, update Firestore.
     */
    private fun handleBitmap(bitmap: Bitmap) {
        CloudinaryHelper.uploadBitmap(bitmap, onSuccess = { secureUrl ->
            if (secureUrl != null) {
                Log.d(TAG, "handleBitmap: Cloudinary secureUrl=$secureUrl")

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val updatedUser = User(
                        id = currentUser.uid,
                        name = currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
                        password = "",
                        imageUrl = secureUrl,
                        country = "" // if needed
                    )
                    UserRepository().updateUser(updatedUser) { success, error ->
                        if (success) {
                            Log.d(TAG, "handleBitmap: Firestore user updated with imageUrl=$secureUrl")
                            Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                            // Refresh from Firestore or just directly update viewmodel
                            sharedUserViewModel.currentUserData.value = updatedUser
                        } else {
                            Log.d(TAG, "handleBitmap: Firestore updateUser failed: $error")
                            Toast.makeText(requireContext(), "Failed to update user: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.d(TAG, "handleBitmap: currentUser is null.")
                }

                // Immediate UI feedback if you want to see it right away
                Glide.with(this)
                    .load(secureUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(720, 720)
                    .centerCrop()
                    .into(binding.profileImage)

            } else {
                Log.d(TAG, "handleBitmap: secureUrl is null!")
            }
        }, onError = { error ->
            Log.d(TAG, "handleBitmap: Cloudinary upload failed: $error")
            Toast.makeText(requireContext(), "Upload failed: $error", Toast.LENGTH_SHORT).show()
        })
    }

    /**
     * Loads a Bitmap from the given URI, then calls handleBitmap() to do the Cloudinary flow.
     */
    private fun handleImageUri(uri: Uri) {
        val bitmap: Bitmap? = context?.contentResolver?.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
        if (bitmap != null) {
            handleBitmap(bitmap)
        } else {
            Toast.makeText(requireContext(), "Failed to load image from URI", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * A dialog to pick from gallery or take a photo.
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

    private fun clearLocalData() {
        val currentUserId = auth.currentUser?.uid
        currentUserId?.let { userId ->
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val dishDao = AppDatabase.getDatabase(requireContext()).dishDao()
                    val dishesByUser = dishDao.getDishesByUserSync(userId)
                    if (dishesByUser.isNotEmpty()) {
                        dishDao.deleteDishes(dishesByUser)
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to clear local data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
