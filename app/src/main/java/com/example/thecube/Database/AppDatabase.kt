package com.example.thecube.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.thecube.model.Dish

@Database(entities= [Dish::class], version = 1)
abstract  class AppDatabase:RoomDatabase() {
    abstract fun dishDao():DishDao

    companion object{
       @Volatile private var instance:AppDatabase? = null

        fun getInstace(context: Context):AppDatabase{
            return instance?: synchronized(this){
instance ?:Room.databaseBuilder(context.applicationContext,AppDatabase::class.java,"TheCube_Database").build().also { instance = it }
            }

        }
    }
}