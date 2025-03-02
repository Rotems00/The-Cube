package com.example.thecube.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.thecube.model.Dish
import com.example.thecube.repository.RecipeRepository
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val recipeRepository = RecipeRepository()

    // LiveData for the dish fetched from the remote API
    private val _dishes = MutableLiveData<List<Dish>>()
    val dishes: LiveData<List<Dish>> get() = _dishes

    // Function to load a random dish from the API
    fun loadRandomMeals(count: Int = 5) {
        viewModelScope.launch {
            val list = mutableListOf<Dish>()
            repeat(count) {
                val dish = recipeRepository.fetchRandomDish()
                dish?.let { list.add(it) }
            }
            _dishes.postValue(list)
        }
    }
}

