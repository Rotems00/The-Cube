package com.example.thecube.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.thecube.databinding.FragmentCommentBinding
import com.example.thecube.model.Comment
import com.example.thecube.local.AppDatabase
import com.example.thecube.repository.CommentRepository
import com.example.thecube.ui.CommentAdapter
import com.example.thecube.viewModel.SharedUserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CommentFragment : Fragment() {

    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentRepository: CommentRepository
    private lateinit var commentAdapter: CommentAdapter

    // Dish ID passed via Bundle (from DishDetailFragment or ProfileFragment)
    private val dishId: String by lazy { arguments?.getString("dishId") ?: "" }

    // Shared ViewModel holding Firestore user data.
    private val sharedUserViewModel: SharedUserViewModel by activityViewModels()

    // Firestore instance for optional real-time updates.
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize CommentRepository with the Room DAO.
        val commentDao = AppDatabase.getDatabase(requireContext()).commentDao()
        commentRepository = CommentRepository(commentDao)

        // Set up RecyclerView for comments.
        commentAdapter = CommentAdapter()
        binding.recyclerViewComments.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.recyclerViewComments.adapter = commentAdapter

        // Trigger a one-time sync: fetch comments from Firestore for this dish and insert into Room.
        lifecycleScope.launch {
            commentRepository.syncCommentsFromFirestore(dishId)
        }

        // OPTIONAL: Start real-time listening for changes in Firestore.
        startListeningForComments(dishId)

        // Observe the comments LiveData for the dish.
        commentRepository.getCommentsForDish(dishId).observe(viewLifecycleOwner) { comments ->
            Log.d("CommentFragment", "Loaded ${comments.size} comments for dish $dishId")
            commentAdapter.updateComments(comments)
        }

        binding.buttonPostComment.setOnClickListener {
            val commentText = binding.editTextComment.text.toString().trim()
            if (commentText.isEmpty()) {
                Log.d("CommentFragment", "Empty comment; nothing to post.")
                return@setOnClickListener
            }

            // Ensure we have user data loaded.
            ensureUserDataLoaded { user ->
                val userId = user.id
                val userName = if (user.name.isNotEmpty()) user.name else "Anonymous"
                val userImageUrl = user.imageUrl

                Log.d("CommentFragment", "Posting comment as userId: $userId, dishId: $dishId, imageUrl: $userImageUrl")

                // Generate a new comment ID using Firestore's mechanism.
                val commentId = FirebaseFirestore.getInstance().collection("comments").document().id

                val newComment = Comment(
                    id = commentId,
                    comment = commentText,
                    userId = userId,
                    userName = userName,
                    timeStamp = System.currentTimeMillis(),
                    userImageUrl = userImageUrl,
                    dishId = dishId
                )

                lifecycleScope.launch {
                    val success = commentRepository.postComment(newComment)
                    if (success) {
                        Log.d("CommentFragment", "Comment posted with id: $commentId")
                        binding.editTextComment.text?.clear()
                    } else {
                        Log.e("CommentFragment", "Failed to post comment.")
                    }
                }
            }
        }
    }

    /**
     * Checks if SharedUserViewModel contains user data.
     * If not, logs the current FirebaseAuth user uid and loads it from Firestore using that uid.
     * Once loaded, calls the provided onUserLoaded callback.
     */
    private fun ensureUserDataLoaded(onUserLoaded: (com.example.thecube.model.User) -> Unit) {
        val currentUser = sharedUserViewModel.currentUserData.value
        if (currentUser != null) {
            // User data is already loaded.
            Log.d("CommentFragment", "User data already loaded: $currentUser")
            onUserLoaded(currentUser)
        } else {
            // Log FirebaseAuth current user uid.
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            Log.d("CommentFragment", "Firebase user on device: ${firebaseUser?.uid}")
            if (firebaseUser == null) {
                Toast.makeText(requireContext(), "No authenticated user found.", Toast.LENGTH_SHORT).show()
                return
            }
            // Fetch the user data from Firestore.
            val userDocRef = FirebaseFirestore.getInstance().collection("users")
                .document(firebaseUser.uid)
            Log.d("CommentFragment", "Fetching user document for uid: ${firebaseUser.uid}")
            userDocRef.get()
                .addOnSuccessListener { snapshot ->
                    Log.d("CommentFragment", "Firestore get() successful. Snapshot exists? ${snapshot.exists()}")
                    if (snapshot.exists()) {
                        val loadedUser = snapshot.toObject(com.example.thecube.model.User::class.java)
                        if (loadedUser != null) {
                            Log.d("CommentFragment", "Loaded user: $loadedUser")
                            sharedUserViewModel.currentUserData.value = loadedUser
                            onUserLoaded(loadedUser)
                        } else {
                            Log.e("CommentFragment", "User data parsing failed. Snapshot data: ${snapshot.data}")
                            Toast.makeText(requireContext(), "User data could not be parsed.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("CommentFragment", "User document does not exist for uid: ${firebaseUser.uid}")
                        Toast.makeText(requireContext(), "User document does not exist.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CommentFragment", "Failed to load user data: ${e.message}")
                    Toast.makeText(requireContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Starts a real-time snapshot listener on Firestore for comments of the given dish.
     * This optional feature updates the local Room database when changes occur remotely.
     */
    private fun startListeningForComments(dishId: String) {
        firestore.collection("comments")
            .whereEqualTo("dishId", dishId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CommentFragment", "Error listening for comments: ${error.message}")
                    return@addSnapshotListener
                }
                snapshot?.let { snap ->
                    val remoteComments = snap.documents.mapNotNull { doc ->
                        doc.toObject(Comment::class.java)
                    }
                    lifecycleScope.launch {
                        // Get current local comments synchronously.
                        val localComments = commentRepository.getLocalCommentsForDishSync(dishId)
                        val remoteIds = remoteComments.map { it.id }.toSet()

                        // Delete local comments that are no longer in remote.
                        localComments.filter { it.id !in remoteIds }.forEach { comment ->
                            commentRepository.deleteCommentLocally(comment)
                        }

                        // Insert (or update) remote comments.
                        remoteComments.forEach { comment ->
                            commentRepository.insertCommentLocally(comment)
                        }
                    }
                    Log.d("CommentFragment", "Realtime update: Found ${remoteComments.size} comments for dish $dishId")
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
