package com.example.thecube.utils

import com.example.thecube.model.Country
import com.example.thecube.remote.Meal

fun mapMealToCountry(meal: Meal, countries: List<Country>): Country {
    val areaToCountryMap = mapOf(
        "Italian" to "Italy",
        "American" to "United States of America",
        "British" to "United Kingdom",
        "Indian" to "India",
        "Chinese" to "China",
        "French" to "France",
        "Mexican" to "Mexico",
        "Japanese" to "Japan"
        // Add more mappings as needed...
    )

    val mealArea = meal.strArea
    val mappedCountryName = areaToCountryMap[mealArea]
    val matchingCountry = if (mappedCountryName != null) {
        countries.find { it.name.common.equals(mappedCountryName, ignoreCase = true) }
    } else {
        null
    }
    return matchingCountry ?: countries.random()
}
