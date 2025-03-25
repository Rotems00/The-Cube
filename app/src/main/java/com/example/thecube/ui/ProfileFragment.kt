package com.example.thecube.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.thecube.R
import com.example.thecube.databinding.DialogChangePasswordBinding
import com.example.thecube.databinding.FragmentProfileBinding
import com.example.thecube.local.AppDatabase
import com.example.thecube.repository.DishRepository
import com.example.thecube.repository.UserRepository
import com.example.thecube.ui.DishAdapter
import com.example.thecube.viewModel.DishViewModel
import com.example.thecube.viewModel.SharedUserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.MultiBrowseCarouselStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel for current user data.
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()
    // DishViewModel for dish operations.
    private val dishViewModel: DishViewModel by activityViewModels()
    // Adapter for the user's dishes carousel.
    private lateinit var dishAdapter: DishAdapter
    // Repository to load dishes from Room.
    private lateinit var dishRepository: DishRepository
    // Repository for user updates.
    private val userRepository = UserRepository()

    // For picking/capturing a new profile image.
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private var currentPhotoUri: Uri? = null
    // To keep a reference to Picasso targets.
    private val picassoTargets = mutableListOf<Target>()

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Launcher for picking an image from gallery.
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadProfileImage(it)
            }
        }
        // Launcher for capturing a new image via camera.
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentPhotoUri?.let { uri ->
                    val target = object : Target {
                        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                            picassoTargets.remove(this)
                            uploadProfileImage(bitmap)
                        }
                        override fun onBitmapFailed(e: Exception?, errorDrawable: android.graphics.drawable.Drawable?) {
                            picassoTargets.remove(this)
                            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                        }
                        override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {}
                    }
                    picassoTargets.add(target)
                    Picasso.get().load(uri).resize(720, 720).centerCrop().into(target)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe profile data.
        sharedUserViewModel.currentUserData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.profileName.text = if (!user.name.isNullOrEmpty()) user.name else "User Name"
                binding.profileEmail.text = if (!user.email.isNullOrEmpty()) user.email else "user@example.com"
                if (!user.imageUrl.isNullOrEmpty()) {
                    Picasso.get().load(user.imageUrl).into(binding.profileImage)
                } else {
                    binding.profileImage.setImageResource(R.drawable.person_icon)
                }
            } else {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                binding.profileName.text = firebaseUser?.displayName ?: "User Name"
                binding.profileEmail.text = firebaseUser?.email ?: "user@example.com"
                if (firebaseUser?.photoUrl != null) {
                    Picasso.get().load(firebaseUser.photoUrl).into(binding.profileImage)
                } else {
                    binding.profileImage.setImageResource(R.drawable.person_icon)
                }
            }
        }

        // Fetch user data from Firestore if not already loaded.
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (!userId.isNullOrEmpty() && sharedUserViewModel.currentUserData.value == null) {
            loadUserFromFirestore(userId)
        }

        // Logout button.
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            sharedUserViewModel.currentUserData.value = null
            findNavController().navigate(R.id.splashFragment)
        }

        // Profile picture editing.
        binding.profileImage.setOnClickListener {
            showImageSourceDialog()
        }

        // Username editing.
        binding.profileName.setOnClickListener {
            showEditUsernameDialog()
        }

        // Change Password click listener.
        binding.changePasswordText.setOnClickListener {
            showChangePasswordDialog()
        }

        // -----------------------------
        // Dish Carousel Setup Section
        // -----------------------------
        // Initialize dish repository.
        val dishDao = AppDatabase.getDatabase(requireContext()).dishDao()
        dishRepository = DishRepository(dishDao)

        // Observe dishes from Room and update the shared ViewModel.
        if (!userId.isNullOrEmpty()) {
            dishRepository.getDishesByUserFromRoom(userId).observe(viewLifecycleOwner) { dishes ->
                Log.d("ProfileFragment", "Fetched ${dishes.size} dishes for user: $userId")
                sharedUserViewModel.userDishes.value = dishes
            }
        }

        // 1. Set up the carousel layout manager.
        val carouselLayoutManager = CarouselLayoutManager(MultiBrowseCarouselStrategy())
        binding.recyclerViewUserDishes.layoutManager = carouselLayoutManager

        // 2. Attach a snap helper to the RecyclerView.
        val snapHelper = CarouselSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewUserDishes)

        // 3. Create the DishAdapter with callbacks.
        dishAdapter = DishAdapter(
            onItemClick = { dish ->
                // Navigate to edit if it belongs to current user.
                if (dish.userId == FirebaseAuth.getInstance().currentUser?.uid) {
                    val action = ProfileFragmentDirections.actionProfileFragmentToEditDishFragment(dish)
                    findNavController().navigate(action)
                } else {
                    // Otherwise navigate to dish detail view.
                    val bundle = Bundle().apply { putParcelable("dish", dish) }
                    findNavController().navigate(R.id.dishDetailFragment, bundle)
                }
            },
            onLikeClicked = { updatedDish ->
                lifecycleScope.launch {
                    dishViewModel.updateDish(updatedDish)
                }
            }
        )

        // 4. Set the adapter on the RecyclerView.
        binding.recyclerViewUserDishes.adapter = dishAdapter

        if (sharedUserViewModel.countriesData.value.isNullOrEmpty()) {
            sharedUserViewModel.fetchCountries()
        }

        // Observe countries data and update the adapter.
        sharedUserViewModel.countriesData.observe(viewLifecycleOwner) { countriesList ->
            if (!countriesList.isNullOrEmpty()) {
                dishAdapter.setCountries(countriesList)
                Log.d("ProfileFragment", "Updated DishAdapter with ${countriesList.size} countries")
            }
        }

        // 5. Observe user dishes from the shared ViewModel and update the adapter.
        sharedUserViewModel.userDishes.observe(viewLifecycleOwner) { dishes ->
            dishAdapter.submitList(dishes)
        }
        // -----------------------------
        // End Dish Carousel Setup Section
        // -----------------------------
    }

    private fun showChangePasswordDialog() {
        // Inflate the dialog layout using binding.
        val dialogBinding = DialogChangePasswordBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setView(dialogBinding.root)
            .setPositiveButton("Change") { dialog, _ ->
                val newPassword = dialogBinding.editTextNewPassword.text.toString()
                val confirmPassword = dialogBinding.editTextConfirmPassword.text.toString()
                if (newPassword.isNotBlank() && newPassword == confirmPassword) {
                    FirebaseAuth.getInstance().currentUser?.updatePassword(newPassword)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Opens a dialog to choose between camera and gallery.
    private fun showImageSourceDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Profile Picture")
            .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                when (which) {
                    0 -> {
                        val photoFile = com.example.thecube.utils.createImageFile(requireContext())
                        currentPhotoUri = androidx.core.content.FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.fileprovider",
                            photoFile
                        )
                        currentPhotoUri?.let { uri ->
                            cameraLauncher.launch(uri)
                        }
                    }
                    1 -> {
                        imagePickerLauncher.launch("image/*")
                    }
                }
            }
            .show()
    }

    // Overloaded method to upload image from a Uri.
    private fun uploadProfileImage(uri: Uri) {
        Picasso.get().load(uri).resize(720, 720).centerCrop().into(object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                uploadProfileImage(bitmap)
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: android.graphics.drawable.Drawable?) {
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
            }
            override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {}
        })
    }

    // Overloaded method to upload image from a Bitmap.
    private fun uploadProfileImage(bitmap: Bitmap) {
        com.example.thecube.utils.CloudinaryHelper.uploadBitmap(bitmap,
            onSuccess = { secureUrl ->
                Picasso.get().load(secureUrl).into(binding.profileImage)
                Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                updateUserProfilePic(secureUrl!!)
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Error uploading image: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Update user profile picture URL in Firestore (and Room).
    private fun updateUserProfilePic(newUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userRepository.updateUserProfilePic(userId, newUrl) { success ->
            if (success) {
                sharedUserViewModel.currentUserData.value?.let { user ->
                    user.imageUrl = newUrl
                    sharedUserViewModel.currentUserData.postValue(user)
                }
                Log.d("ProfileFragment", "Profile picture updated in database")
            } else {
                Toast.makeText(requireContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Shows a dialog to edit the username.
    private fun showEditUsernameDialog() {
        val editText = EditText(requireContext())
        editText.setText(binding.profileName.text)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Username")
            .setView(editText)
            .setPositiveButton("Save") { dialog, _ ->
                val newUsername = editText.text.toString().trim()
                if (newUsername.isNotEmpty()) {
                    updateUsername(newUsername)
                } else {
                    Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Update username in Firestore (and Room).
    private fun updateUsername(newUsername: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userRepository.updateUsername(userId, newUsername) { success ->
            if (success) {
                binding.profileName.text = newUsername
                sharedUserViewModel.currentUserData.value?.let { user ->
                    user.name = newUsername
                    sharedUserViewModel.currentUserData.postValue(user)
                }
                Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to update username", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load user data from Firestore.
    private fun loadUserFromFirestore(userId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.toObject(com.example.thecube.model.User::class.java)
                    if (user != null) {
                        sharedUserViewModel.currentUserData.value = user
                        Log.d("ProfileFragment", "Fetched user from Firestore: $user")
                    } else {
                        Log.e("ProfileFragment", "User is null in Firestore doc.")
                    }
                } else {
                    Log.e("ProfileFragment", "User document does not exist for $userId.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Failed to fetch user: ${e.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
