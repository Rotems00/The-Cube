package com.example.thecube.model

data class Comment(
    val id: String = "",
    val comment: String = "",
    val userId: String = "",
    val userName: String = "",
    val timeStamp: Long = 0,
    val dishId: String = ""
)
