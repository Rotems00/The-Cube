package com.example.thecube.repository
import com.google.firebase.firestore.SetOptions
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.thecube.local.DishDao
import com.example.thecube.model.Dish
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DishRepository(private val dishDao: DishDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "DishRepository"

    suspend fun insertDish(dish: Dish) {
        val dishId = if (dish.id.isEmpty()) firestore.collection("dishes").document().id else dish.id
        val finalDish = dish.copy(id = dishId)

        // Insert into Room
        dishDao.insertDish(finalDish)
        // Save to Firestore (using the same document ID)
        firestore.collection("dishes").document(dishId).set(finalDish)
            .addOnSuccessListener { Log.d(TAG, "Dish inserted successfully in Firestore") }
            .addOnFailureListener { e -> Log.e(TAG, "Firestore insert failed: ${e.message}") }
    }

    suspend fun updateDishLike(updatedDish: Dish) {
        // Update local Room
        dishDao.updateDish(updatedDish)
        Log.d("UpdateDishLike", "Room updated for dish ${updatedDish.id}, countLikes: ${updatedDish.countLikes}")

        // Update Firestore using merge options.
        firestore.collection("dishes").document(updatedDish.id)
            .set(updatedDish, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("UpdateDishLike", "Firestore update successful! New count: ${updatedDish.countLikes}")
            }
            .addOnFailureListener { e ->
                Log.e("UpdateDishLike", "Firestore update failed", e)
            }
    }
    fun getDishesByUserFromRoom(userId: String): LiveData<List<Dish>> =
        dishDao.getDishesByUser(userId)

    fun getDishesByDifficultyAndType(difficulty: String, typeDish: String): LiveData<List<Dish>> =
        dishDao.getDishesByDifficultyAndType(difficulty, typeDish)


    fun getDishesByCountryAndFilters(country: String, difficulty: String, typeDish: String): LiveData<List<Dish>> =
        dishDao.getDishesByCountryAndFilters(country, difficulty, typeDish)





    suspend fun syncDishesFromFirestore() {
        try {
            val snapshot = firestore.collection("dishes").get().await()
            val dishes = snapshot.documents.mapNotNull { document ->
                // Convert the document to a Dish, then adjust the fields
                val firestoreDish = document.toObject(Dish::class.java)
                val firestoreLikedBy = document["likedBy"] as? List<String> ?: emptyList()
                val firestoreCount = document["countLikes"] as? Long

                firestoreDish?.copy(
                    id = document.id,
                    likedBy = firestoreLikedBy,
                    // If Firestore doesn't store countLikes, fallback to size of likedBy
                    countLikes = firestoreCount?.toInt() ?: firestoreLikedBy.size
                )
            }

            dishes.forEach { dish ->
                dishDao.insertDish(dish)
                Log.d(TAG, "Dish synced: ${dish.dishName}, likedBy: ${dish.likedBy}, countLikes: ${dish.countLikes}")
            }
            Log.d(TAG, "Successfully synced ${dishes.size} dishes from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing dishes from Firestore: ${e.message}")
        }
    }


    suspend fun syncDishesByCountry(country: String) {
        try {
            val snapshot = firestore.collection("dishes")
                .whereEqualTo("country", country)
                .get()
                .await()

            val firestoreDishes = snapshot.documents.mapNotNull { document ->
                document.toObject(Dish::class.java)?.copy(
                    id = document.id,
                    likedBy = document["likedBy"] as? List<String> ?: emptyList(),
                    countLikes = (document["likedBy"] as? List<*>)?.size ?: 0
                )
            }

            firestoreDishes.forEach { remoteDish ->
                val localDish = dishDao.getDishById(remoteDish.id)
                val mergedLikes = (localDish?.likedBy ?: emptyList()) + remoteDish.likedBy
                val distinctMergedLikes = mergedLikes.distinct()
                val finalDish = remoteDish.copy(
                    likedBy = distinctMergedLikes,
                    countLikes = distinctMergedLikes.size
                )
                Log.d(TAG, "Syncing ${finalDish.dishName}: localLikes=${localDish?.likedBy}, remoteLikes=${remoteDish.likedBy}, mergedLikes=${finalDish.likedBy}")
                dishDao.insertDish(finalDish)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing dishes by country: ${e.message}")
        }
    }

    suspend fun updateDish(dish: Dish) {
        dishDao.updateDish(dish)
        firestore.collection("dishes").document(dish.id).set(dish)
            .addOnSuccessListener { Log.d(TAG, "Dish updated successfully.") }
            .addOnFailureListener { Log.e(TAG, "Error updating dish: ${it.message}") }
    }

    suspend fun deleteDish(dish: Dish) {
        dishDao.deleteDish(dish)
        firestore.collection("dishes").document(dish.id).delete()
            .addOnSuccessListener { Log.d(TAG, "Dish deleted successfully.") }
            .addOnFailureListener { Log.e(TAG, "Error deleting dish: ${it.message}") }
    }

    suspend fun syncLocalToFirestore() {
        val localDishes = dishDao.getAllDishesSync()
        localDishes.forEach { dish ->
            firestore.collection("dishes").document(dish.id).set(dish)
                .addOnSuccessListener { Log.d(TAG, "Dish synced to Firestore: ${dish.dishName}") }
                .addOnFailureListener { Log.e(TAG, "Failed syncing dish: ${dish.dishName}, ${it.message}") }
        }
    }

    fun getDishesByCountryFromRoom(country: String): LiveData<List<Dish>> =
        dishDao.getDishesByCountry(country)

    companion object {
        private const val TAG = "DishRepository"
    }
}
