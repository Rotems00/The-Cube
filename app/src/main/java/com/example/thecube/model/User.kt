package com.example.thecube.model

data class User(
    val id: String = "",
    var name: String = "",
    val email: String = "",
    val password: String = "",
    var imageUrl: String = "",
    val country: String = ""
)
