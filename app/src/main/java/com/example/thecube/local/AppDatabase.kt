package com.example.thecube.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.thecube.model.Dish
import kotlin.concurrent.Volatile


@Database(entities = [Dish::class], version =1, exportSchema = false)
abstract class AppDatabase:RoomDatabase(){
    abstract fun dishDao(): DishDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase?=null

        fun getDatabase(context:Context):AppDatabase{
            return INSTANCE?: synchronized(this){
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}