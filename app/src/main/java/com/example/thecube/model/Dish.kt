package com.example.thecube.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "dishes")
data class Dish(
    @PrimaryKey val id: String = "",
    val flagImageUrl: String = "",
    val dishName: String = "",
    val dishDescription: String = "",
    val dishSteps: String = "",
    val imageUrl: String = "",
    val ingredients: String = "",
    val country: String = "",
    val userId: String = "",
    val countLikes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val typeDish : String = "",
    val difficulty : String = "Medium",

    ) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", "", "", 0, emptyList(), "Regular", "Medium")
}