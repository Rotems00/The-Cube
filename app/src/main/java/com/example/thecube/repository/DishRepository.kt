package com.example.thecube.repository

import android.util.Log
import com.example.thecube.model.Dish
import androidx.lifecycle.LiveData
import com.example.thecube.local.DishDao
import com.google.firebase.firestore.FirebaseFirestore

class DishRepository(private val dishDao: DishDao) {

    val allDishes: LiveData<List<Dish>> = dishDao.getAllDishes()

    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "DishRepository"

    suspend fun insertDish(dish: Dish) {
        Log.d(TAG, "Inserting dish: ${dish.dishName} with country: ${dish.country}")
        dishDao.insertDish(dish)
        firestore.collection("dishes").document(dish.id)
            .set(dish)
            .addOnSuccessListener {
                Log.d(TAG, "Dish ${dish.id} successfully written to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error writing dish ${dish.id} to Firestore: ${e.message}")
            }
    }

    suspend fun updateDish(dish: Dish) {
        Log.d(TAG, "Updating dish: ${dish.dishName} with country: ${dish.country}")
        dishDao.updateDish(dish)
        firestore.collection("dishes").document(dish.id)
            .set(dish)
            .addOnSuccessListener {
                Log.d(TAG, "Dish ${dish.id} successfully updated in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating dish ${dish.id} in Firestore: ${e.message}")
            }
    }

    suspend fun deleteDish(dish: Dish) {
        Log.d(TAG, "Deleting dish: ${dish.dishName} with id: ${dish.id}")
        dishDao.deleteDish(dish)
        firestore.collection("dishes").document(dish.id)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Dish ${dish.id} successfully deleted from Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting dish ${dish.id} from Firestore: ${e.message}")
            }
    }

    fun getDishesByCountry(country: String, onComplete: (List<Dish>) -> Unit) {
        Log.d(TAG, "Fetching dishes for country: $country")
        firestore.collection("dishes")
            .whereEqualTo("country", country)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dishes = querySnapshot.documents.mapNotNull { doc ->
                    val dish = doc.toObject(Dish::class.java)
                    dish?.also {
                        Log.d(TAG, "Retrieved dish: ${it.dishName} with country: ${it.country}")
                    }
                }
                Log.d(TAG, "Total dishes fetched for $country: ${dishes.size}")
                onComplete(dishes)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching dishes by country: ${e.message}")
                onComplete(emptyList())
            }
    }
}
