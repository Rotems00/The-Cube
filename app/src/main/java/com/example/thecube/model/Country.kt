package com.example.thecube.model

data class Country(
    val name: Name,
    val flags: Flags
)

data class Name(
    val common: String
)

data class Flags(
    val png: String,  // URL for PNG flag
    val svg: String   // URL for SVG flag (optional)
)
