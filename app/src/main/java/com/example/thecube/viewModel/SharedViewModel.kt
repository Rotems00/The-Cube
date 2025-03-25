package com.example.thecube.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thecube.model.Country
import com.example.thecube.model.Dish
import com.example.thecube.model.User
import com.example.thecube.remote.RetrofitInstance
import kotlinx.coroutines.launch

class SharedUserViewModel : ViewModel() {
    // Holds current user data in memory
    val currentUserData = MutableLiveData<User>()
    val countriesData = MutableLiveData<List<Country>>()
    val userDishes = MutableLiveData<List<Dish>>()
    var dishesLoaded: Boolean = false

    fun fetchCountries() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getCountries()
                // Update countriesData LiveData.
                countriesData.value = response
            } catch (e: Exception) {
                Log.e("SharedUserViewModel", "Error fetching countries: ${e.message}")
            }
        }
    }
}