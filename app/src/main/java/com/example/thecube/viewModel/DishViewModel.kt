package com.example.thecube.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.thecube.local.AppDatabase
import com.example.thecube.model.Dish
import com.example.thecube.repository.DishRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DishViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DishRepository
    val allDishes: LiveData<List<Dish>>

    init {
        val dishDao = AppDatabase.getDatabase(application).dishDao()
        repository = DishRepository(dishDao)
        // Change this query if needed.
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        allDishes = dishDao.getDishesByUser(currentUserId)
    }

    fun insertDish(dish: Dish) = viewModelScope.launch {
        repository.insertDish(dish)
    }

    fun updateDish(dish: Dish) = viewModelScope.launch {
        repository.updateDish(dish)
    }

    fun deleteDish(dish: Dish) = viewModelScope.launch {
        repository.deleteDish(dish)
    }

    fun updateDishLike(dish: Dish) = viewModelScope.launch {
        repository.updateDishLike(dish)
    }


    // New method to get favourite dishes (liked by current user)
    fun getFavouriteDishes(currentUserId: String): LiveData<List<Dish>> {
        return AppDatabase.getDatabase(getApplication()).dishDao().getDishesLikedByUser(currentUserId)
    }

    // Sync function remains the same.
    fun syncDishes() = viewModelScope.launch {
        repository.syncDishesFromFirestore()
    }
    fun syncLocalDataToFirestore() = viewModelScope.launch {
        repository.syncLocalToFirestore()
    }
}
