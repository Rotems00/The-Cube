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
import com.example.thecube.R
import com.example.thecube.databinding.FragmentEditDishBinding
import com.example.thecube.local.AppDatabase
import com.example.thecube.model.Dish
import com.example.thecube.repository.CommentRepository
import com.example.thecube.utils.CloudinaryHelper
import com.example.thecube.viewModel.DishViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.launch
import java.io.File

class EditDishFragment : Fragment() {

    private var _binding: FragmentEditDishBinding? = null
    private val binding get() = _binding!!

    private lateinit var dishViewModel: DishViewModel
    private lateinit var currentDish: Dish
    private var updatedImageUrl: String? = null

    // Launchers for gallery and camera
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>

    // List to hold Picasso Targets so they aren’t garbage collected.
    private val picassoTargets = mutableListOf<Target>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uploadImage(it) }
        }
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

        // Retrieve the dish passed as an argument.
        currentDish = requireArguments().getParcelable("dish")!!
        updatedImageUrl = currentDish.imageUrl

        // Populate fields.
        binding.editTextDishName.setText(currentDish.dishName)
        binding.editTextDishDescription.setText(currentDish.dishDescription)
        binding.editTextIngredients.setText(currentDish.ingredients)
        binding.editTextSteps.setText(currentDish.dishSteps)

        // Load current image.
        Picasso.get()
            .load(currentDish.imageUrl)
            .resize(720, 720)
            .centerCrop()
            .into(binding.imageViewDishUpload)

        // Allow user to change image.
        binding.imageViewDishUpload.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // "View Comments" button: pass dish id to CommentFragment using the explicit action.
        binding.buttonViewComments.setOnClickListener {
            val bundle = Bundle().apply { putString("dishId", currentDish.id) }
            findNavController().navigate(R.id.action_editDishFragment_to_commentFragment, bundle)
        }

        // Update dish button.
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
                findNavController().navigate(R.id.profileFragment)
            }
        }

        // Show delete button if user is creator.
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
                            // Create a CommentRepository instance by passing CommentDao:
                            val commentRepo = CommentRepository(AppDatabase.getDatabase(requireContext()).commentDao())
                            val success = commentRepo.deleteCommentsForDish(currentDish.id)
                            if (success) {
                                Toast.makeText(requireContext(), "Dish and comments deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Failed to delete comments", Toast.LENGTH_SHORT).show()
                            }
                            findNavController().navigate(R.id.profileFragment)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

        } else {
            binding.buttonDeleteDish.visibility = View.GONE
        }
    }

    // Loads an image from the gallery URI and uploads it.
    private fun uploadImage(uri: Uri) {
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

    // Uploads the bitmap via Cloudinary and updates the ImageView.
    private fun uploadCapturedImage(bitmap: Bitmap) {
        CloudinaryHelper.uploadBitmap(bitmap, onSuccess = { secureUrl ->
            updatedImageUrl = secureUrl
            Picasso.get()
                .load(secureUrl)
                .resize(720, 720)
                .centerCrop()
                .into(binding.imageViewDishUpload)
            Toast.makeText(requireContext(), "Image updated successfully", Toast.LENGTH_SHORT).show()
        }, onError = { error ->
            Toast.makeText(requireContext(), "Image update failed: $error", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
