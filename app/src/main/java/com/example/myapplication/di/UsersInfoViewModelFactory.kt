package com.example.myapplication.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.viewmodel.UsersInfoViewModel

class UsersInfoViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UsersInfoViewModel(userRepository) as T
    }
}