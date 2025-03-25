package com.example.thecube.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey val id: String = "",
    val comment: String = "",
    val userId: String = "",
    val userName: String = "",
    val timeStamp: Long = 0,
    val userImageUrl: String = "",
    val dishId: String = ""
)
