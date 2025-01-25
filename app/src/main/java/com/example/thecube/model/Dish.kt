package com.example.thecube.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Dish")
data class Dish(
   @PrimaryKey val id: String,
    val dishName: String,
    val dishDescription: String,
    val imageUrl : String,
    val rating:Float,
    val countLikes: Int,
    val ingredients: String,
    val steps : String
)
