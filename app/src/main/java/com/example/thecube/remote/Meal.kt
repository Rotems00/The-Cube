package com.example.thecube.remote



data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strInstructions: String,
    val strMealThumb: String,
    val strArea: String // e.g., "Italian", "American", etc.
)
