package com.example.thecube.local

import com.example.thecube.model.Dish

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface DishDao {
    //Read all our dishes

    @Query("SELECT * FROM dishes")
    fun getAllDishes(): LiveData<List<Dish>>

    @Query("SELECT * FROM dishes WHERE userId = :userId")
    suspend fun getDishesByUserSync(userId: String): List<Dish>
    //Insert a single dish

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: Dish)

    @Query("SELECT * FROM dishes WHERE userId = :userId")
    fun getDishesByUser(userId: String): LiveData<List<Dish>>

    // Update an existing dish
    @Update
    suspend fun updateDish(dish: Dish)

    //Delete a dish
    @Delete
    suspend fun deleteDish(dish: Dish)

    @Delete
    suspend fun deleteDishes(dishes: List<Dish>)
}


