package com.example.thecube.repository



import com.example.thecube.model.Dish
import com.example.thecube.remote.Meal
import com.example.thecube.remote.RecipeApiService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecipeRepository {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.themealdb.com/api/json/v1/1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: RecipeApiService = retrofit.create(RecipeApiService::class.java)

    // Fetch a random meal from the API and map it to Dish
    suspend fun fetchRandomDish(): Dish? {
        return try {
            val response = apiService.getRandomMeal()
            val meal = response.meals?.firstOrNull()
            meal?.let { mapMealToDish(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun mapMealToDish(meal: Meal): Dish {
        return Dish(
            id = meal.idMeal,
            dishName = meal.strMeal,
            dishDescription = meal.strInstructions,
            imageUrl = meal.strMealThumb,
            flagImageUrl = "unknown",
            countLikes = 0,
            ingredients = "unknown",
        )
    }
}
