package com.example.thecube.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "dishes")
data class Dish(
    @PrimaryKey val id: String,
    val flagImageUrl: String,
    val dishName: String,
    val dishDescription: String,  // Main description
    val dishSteps: String,          // Separate steps field
    val imageUrl: String,
    val countLikes: Int,
    val ingredients: String,
    val country: String,
    val userId: String,

) : Parcelable


