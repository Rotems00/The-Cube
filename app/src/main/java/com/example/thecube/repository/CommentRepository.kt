package com.example.thecube.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.thecube.local.CommentDao
import com.example.thecube.model.Comment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CommentRepository(private val commentDao: CommentDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "CommentRepository"

    // Returns comments from Room as LiveData
    fun getCommentsForDish(dishId: String): LiveData<List<Comment>> {
        return commentDao.getCommentsForDish(dishId)
    }

    suspend fun insertCommentLocally(comment: Comment) {
        commentDao.insertComment(comment)
    }
    suspend fun getLocalCommentsForDishSync(dishId: String): List<Comment> {
        return commentDao.getCommentsForDishSync(dishId)
    }

    suspend fun deleteCommentLocally(comment: Comment) {
        commentDao.deleteComment(comment)
    }




    suspend fun syncAllCommentsFromFirestore() {
        try {
            // 1) Fetch all comments from Firestore
            val snapshot = firestore.collection("comments").get().await()

            // 2) Convert to a list of Comment objects
            val remoteComments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comment::class.java)
            }

            // 3) Clear local comments (optional: or do a merge if you want partial sync)
            commentDao.deleteAllComments()

            // 4) Insert fetched comments into Room
            commentDao.insertComments(remoteComments)

            Log.d(TAG, "Successfully synced ${remoteComments.size} comments from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing comments: ${e.message}")
        }
    }

    // Syncs comments from Firestore into Room
    suspend fun syncCommentsFromFirestore(dishId: String) {
        try {
            val snapshot = firestore.collection("comments")
                .whereEqualTo("dishId", dishId)
                .get()
                .await()

            val remoteComments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comment::class.java)
            }

            // Save each remote comment into Room
            for (comment in remoteComments) {
                commentDao.insertComment(comment)
            }
            Log.d(TAG, "Synced ${remoteComments.size} comments from Firestore for dish $dishId")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing comments: ${e.message}")
        }
    }

    // Post a new comment to Firestore and insert into Room
    suspend fun postComment(comment: Comment): Boolean {
        return try {
            firestore.collection("comments")
                .document(comment.id)
                .set(comment)
                .await()
            // Also store locally
            commentDao.insertComment(comment)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post comment: ${e.message}")
            false
        }
    }

    // Delete comments for a given dish from Firestore and Room
    suspend fun deleteCommentsForDish(dishId: String): Boolean {
        return try {
            // Delete from Firestore using a batch
            val snapshot = firestore.collection("comments")
                .whereEqualTo("dishId", dishId)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()

            // Delete from Room
            commentDao.deleteCommentsForDish(dishId)

            Log.d(TAG, "Deleted comments for dish $dishId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting comments: ${e.message}")
            false
        }
    }
}
