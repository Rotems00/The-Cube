package com.example.thecube.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.thecube.model.Dish

@Dao
interface DishDao {
    //Read all our dishes

    @Query("SELECT * FROM dishes")
    fun getAllDishes(): LiveData<List<Dish>>


    //Insert a single dish

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: Dish)


    // Update an existing dish
    @Update
    suspend fun updateDish(dish: Dish)

    //Delete a dish
    @Delete
    suspend fun deleteDish(dish: Dish)
}


