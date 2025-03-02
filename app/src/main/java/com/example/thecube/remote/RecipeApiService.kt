package com.example.thecube.remote

import retrofit2.http.GET

interface RecipeApiService {
    @GET("random.php")
    suspend fun getRandomMeal(): RecipeResponse
}