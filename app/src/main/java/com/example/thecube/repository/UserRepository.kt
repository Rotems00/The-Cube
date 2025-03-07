package com.example.thecube.repository

import com.example.thecube.model.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    fun createUser(user: User, onComplete: (Boolean, String?) -> Unit) {
        usersCollection.document(user.id)
            .set(user)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    fun updateUser(user: User, onComplete: (Boolean, String?) -> Unit) {
        usersCollection.document(user.id)
            .set(user)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    fun getUser(userId: String, onComplete: (User?) -> Unit) {
        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.toObject(User::class.java)
                onComplete(user)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }
}
