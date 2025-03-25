package com.example.thecube.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromLikedByList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toLikedByList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(",")

}