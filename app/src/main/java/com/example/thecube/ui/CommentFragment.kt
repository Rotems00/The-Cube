package com.example.thecube.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thecube.databinding.FragmentCommentBinding
import com.example.thecube.model.Comment
import com.example.thecube.repository.CommentRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class CommentFragment : Fragment() {

    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!
    private val commentRepository = CommentRepository()
    private lateinit var commentAdapter: CommentAdapter
    private val TAG = "CommentFragment"

    // Dish id passed as an argument (via Safe Args or Bundle)
    private var dishId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dishId = arguments?.getString("dishId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        commentAdapter = CommentAdapter()
        binding.recyclerViewComments.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewComments.adapter = commentAdapter

        loadComments()

        binding.buttonPostComment.setOnClickListener {
            val commentText = binding.editTextComment.text.toString().trim()
            if (commentText.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a comment", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            postComment(commentText)
        }
    }

    private fun loadComments() {
        dishId?.let { id ->
            commentRepository.getCommentsForDish(id) { comments ->
                Log.d(TAG, "Fetched ${comments.size} comments for dishId: $id")
                commentAdapter.updateComments(comments)
            }
        } ?: Log.e(TAG, "Dish ID is null; cannot load comments.")
    }

    private fun postComment(commentText: String) {
        dishId?.let { id ->
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) return@let
            val userId = currentUser.uid
            val userName = currentUser.displayName ?: "Anonymous"  // Retrieve the display name

            val newComment = Comment(
                id = UUID.randomUUID().toString(),
                comment = commentText,
                userId = userId,
                userName = userName,  // Store the username here
                timeStamp = System.currentTimeMillis(),
                dishId = id
            )
            commentRepository.postComment(newComment) { success, error ->
                if (success) {
                    Toast.makeText(requireContext(), "Comment posted", Toast.LENGTH_SHORT).show()
                    binding.editTextComment.text?.clear()
                    loadComments() // Refresh comments after posting.
                } else {
                    Toast.makeText(requireContext(), "Failed to post comment: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
