package com.example.thecube.viewModel
import com.example.thecube.model.Dish
import android.app.Application


import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.thecube.local.AppDatabase
import com.example.thecube.repository.DishRepository
import kotlinx.coroutines.launch

class DishViewModel(application: Application):AndroidViewModel(application) {

    private val repository:DishRepository


    val allDishes:LiveData<List<Dish>>




    init {
        val dishDao = AppDatabase.getDatabase(application).dishDao()

        repository = DishRepository(dishDao)

        allDishes = repository.allDishes
    }

    fun insertDish(dish: Dish) = viewModelScope.launch{
        repository.insertDish(dish)
    }

    fun updateDish(dish: Dish) = viewModelScope.launch{
        repository.updateDish(dish)
    }

    fun deleteDish(dish: Dish) = viewModelScope.launch{
        repository.deleteDish(dish)
    }

}