package com.example.thecube.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.thecube.model.Dish
@Dao
interface DishDao {
    @Query("SELECT * FROM Dish")
    fun getAllDishes():LiveData<List<Dish>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDishes(dishes: List<Dish>)
}