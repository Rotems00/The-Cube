package com.example.thecube.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.thecube.model.Dish
import com.example.thecube.repository.LocalRecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DishViewModel(application: Application) : AndroidViewModel(application) {

    private val _dishes = MutableLiveData<List<Dish>>()
    val dishes: LiveData<List<Dish>> get() = _dishes

    // Load local dishes from your local repository.
    fun loadLocalDishes() {
        CoroutineScope(Dispatchers.IO).launch {
            val localDishes = LocalRecipeRepository.getAllDishes()  // Create this method in your local repository.
            _dishes.postValue(localDishes)
        }
    }
}
