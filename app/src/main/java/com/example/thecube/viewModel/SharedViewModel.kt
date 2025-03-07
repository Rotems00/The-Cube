package com.example.thecube.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.thecube.model.User

class SharedUserViewModel : ViewModel() {
    // Holds current user data in memory
    val currentUserData = MutableLiveData<User>()
}
