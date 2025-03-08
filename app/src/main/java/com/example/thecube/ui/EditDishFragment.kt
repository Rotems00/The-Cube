package com.example.thecube.ui

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.thecube.R
import com.example.thecube.databinding.FragmentEditDishBinding
import com.example.thecube.model.Dish
import com.example.thecube.repository.CommentRepository
import com.example.thecube.viewModel.DishViewModel
import com.example.thecube.repository.UserRepository
import com.example.thecube.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
            uri?.let { uploadImage(it) }
        }
        // Register launcher for camera preview (if needed)
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let { uploadCapturedImage(it) }
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dishViewModel = ViewModelProvider(this).get(DishViewModel::class.java)

        // Retrieve the dish passed as an argument (using Safe Args or Bundle)
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

        // Set click listener to allow image change (gallery-only option for now)
        binding.imageViewDishUpload.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Set click listener for "View Comments" button
        binding.buttonViewComments.setOnClickListener {
            // Create a Bundle with the dishId to pass to the CommentFragment
            val bundle = Bundle().apply {
                putString("dishId", currentDish.id)
            }
            findNavController().navigate(R.id.commentFragment, bundle)
        }

        // Update button listener
        binding.buttonUpdateDish.setOnClickListener {
            val updatedName = binding.editTextDishName.text.toString().trim()
            val updatedDescription = binding.editTextDishDescription.text.toString().trim()
            val updatedIngredients = binding.editTextIngredients.text.toString().trim()
            val updatedSteps = binding.editTextSteps.text.toString().trim()

            if (updatedName.isEmpty() || updatedDescription.isEmpty() ||
                updatedIngredients.isEmpty() || updatedSteps.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create an updated dish instance
            val updatedDish = currentDish.copy(
                dishName = updatedName,
                dishDescription = updatedDescription,
                dishSteps = updatedSteps,
                ingredients = updatedIngredients,
                imageUrl = updatedImageUrl ?: currentDish.imageUrl
            )

            lifecycleScope.launch {
                dishViewModel.updateDish(updatedDish)
                Toast.makeText(requireContext(), "Dish updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.myDishesFragment)
            }
        }

        // Check if the current user is the creator of this dish; if so, show the delete button
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == currentDish.userId) {
            binding.buttonDeleteDish.visibility = View.VISIBLE
            binding.buttonDeleteDish.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Dish")
                    .setMessage("Are you sure you want to delete this dish and all its comments?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            dishViewModel.deleteDish(currentDish)
                            // Delete comments associated with this dish
                            CommentRepository().deleteCommentsForDish(currentDish.id) { success, error ->
                                if (success) {
                                    Toast.makeText(requireContext(), "Dish and comments deleted", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "Failed to delete comments: $error", Toast.LENGTH_SHORT).show()
                                }
                                findNavController().navigate(R.id.myDishesFragment)
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        } else {
            binding.buttonDeleteDish.visibility = View.GONE
        }
    }

    // Use Glide to load image from gallery and then upload it via Cloudinary
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

    // Similar function for camera capture
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
