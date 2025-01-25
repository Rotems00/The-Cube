package com.example.thecube.model

data class Comment(
    val id: String,
    val comment: String,
    val userId: String,
    val timeStamp: Long,
    val dishId: String
)
