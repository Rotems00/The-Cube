package com.example.thecube.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.thecube.model.Dish

@Dao
interface DishDao {

    @Query("SELECT * FROM dishes WHERE userId = :userId")
    suspend fun getDishesByUserSync(userId: String): List<Dish>

    @Query("SELECT * FROM dishes")
    fun getAllDishes(): LiveData<List<Dish>>

    @Query("SELECT * FROM dishes WHERE userId = :userId")
    fun getDishesByUser(userId: String): LiveData<List<Dish>>

    @Query("SELECT * FROM dishes WHERE likedBy <> '' AND (',' ||  likedBy || ',') LIKE '%,' || :userId || ',%'")
    fun getDishesLikedByUser(userId: String): LiveData<List<Dish>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: Dish)

    @Update
    suspend fun updateDish(dish: Dish)

    @Query("SELECT * FROM dishes WHERE country = :country AND difficulty = :difficulty AND typeDish = :typeDish")
    fun getDishesByCountryAndFilters(country: String, difficulty: String, typeDish: String): LiveData<List<Dish>>

    @Query("SELECT * FROM dishes WHERE id = :id LIMIT 1")
    suspend fun getDishById(id: String): Dish?

    @Query("SELECT * FROM dishes WHERE difficulty = :difficulty")
    fun getDishesByDifficulty(difficulty: String): LiveData<List<Dish>>

    @Query("SELECT * FROM dishes WHERE typeDish = :typeDish")
    fun getDishesByType(typeDish: String): LiveData<List<Dish>>

    @Query("SELECT * FROM dishes WHERE difficulty = :difficulty AND typeDish = :typeDish")
    fun getDishesByDifficultyAndType(difficulty: String, typeDish: String): LiveData<List<Dish>>

    @Delete
    suspend fun deleteDish(dish: Dish)

    @Delete
    suspend fun deleteDishes(dishes: List<Dish>)

    @Query("SELECT * FROM dishes")
    suspend fun getAllDishesSync(): List<Dish>

    @Query("SELECT * FROM dishes WHERE country = :country")
    fun getDishesByCountry(country: String): LiveData<List<Dish>>
}
