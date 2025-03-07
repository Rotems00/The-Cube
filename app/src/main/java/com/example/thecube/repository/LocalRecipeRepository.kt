package com.example.thecube.repository

import com.example.thecube.model.Dish




object LocalRecipeRepository {

    // Hardcoded list of dishes with the exact country names and a test user id.
    private val dishes = listOf(
        // United States
        Dish(
            id = "us1",
            dishName = "Cheeseburger",
            dishDescription = "Juicy cheeseburger with all the fixings.",
            dishSteps = "SOMETHING",
            imageUrl = "https://example.com/images/cheeseburger.jpg",
            flagImageUrl = "https://flagcdn.com/w320/us.png",
            countLikes = 150,
            ingredients = "Beef, bun, cheese, lettuce, tomato",
            country = "United States",
            userId = "testUser",

        ),
        Dish(
            id = "us2",
            dishName = "Hot Dog",
            dishDescription = "Classic American hot dog.",
            dishSteps = "SOMETHING",
            imageUrl = "https://example.com/images/hotdog.jpg",
            flagImageUrl = "https://flagcdn.com/w320/us.png",
            countLikes = 120,
            ingredients = "Sausage, bun, mustard, ketchup",
            country = "United States",
            userId = "testUser",

        ),
        // ... update every dish similarly
    )

    fun getDishesByCountry(country: String): List<Dish> {
        return dishes.filter { it.country.equals(country, ignoreCase = true) }
    }

    fun getAllDishes(): List<Dish> {
        return dishes
    }
}
