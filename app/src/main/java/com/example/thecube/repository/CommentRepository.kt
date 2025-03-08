package com.example.thecube.repository

import android.util.Log
import com.example.thecube.model.Comment
import com.google.firebase.firestore.FirebaseFirestore

class CommentRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "CommentRepository"

    fun postComment(comment: Comment, onComplete: (Boolean, String?) -> Unit) {
        firestore.collection("comments")
            .document(comment.id)
            .set(comment)
            .addOnSuccessListener {
                Log.d(TAG, "Comment ${comment.id} successfully posted")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error posting comment: ${e.message}")
                onComplete(false, e.message)
            }
    }

    fun getCommentsForDish(dishId: String, onComplete: (List<Comment>) -> Unit) {
        firestore.collection("comments")
            .whereEqualTo("dishId", dishId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val comments = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)
                }
                Log.d(TAG, "Fetched ${comments.size} comments for dishId: $dishId")
                onComplete(comments)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching comments: ${e.message}")
                onComplete(emptyList())
            }
    }

    fun deleteCommentsForDish(dishId: String, onComplete: (Boolean, String?) -> Unit) {
        firestore.collection("comments")
            .whereEqualTo("dishId", dishId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = firestore.batch()
                for (doc in querySnapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { e -> onComplete(false, e.message) }
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

}
