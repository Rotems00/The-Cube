package com.example.thecube.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dishes")
data class Dish(
   @PrimaryKey val id: String,
   val flagImageUrl: String,
    val dishName: String,
    val dishDescription: String,
    val imageUrl : String,
    val countLikes: Int,
    val ingredients: String,

)
