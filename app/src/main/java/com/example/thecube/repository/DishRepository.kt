package com.example.thecube.repository

import androidx.lifecycle.LiveData
import com.example.thecube.local.DishDao
import com.example.thecube.model.Dish
import com.google.firebase.firestore.FirebaseFirestore

class DishRepository(private val dishDao: DishDao) {

    val allDishes:LiveData<List<Dish>> = dishDao.getAllDishes()

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun  insertDish(dish: Dish){
        dishDao.insertDish(dish)
        firestore.collection("dishes").document(dish.id).set(dish)
    }


    suspend fun updateDish(dish: Dish){
        dishDao.updateDish(dish)
        firestore.collection("dishes").document(dish.id).set(dish)
    }

    suspend fun deleteDish(dish: Dish){
        dishDao.deleteDish(dish)
        firestore.collection("dishes").document(dish.id).delete()
    }

}