package com.example.myapplication.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.DutyPlaceRepository
import com.example.myapplication.data.repository.DutyPlanRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.viewmodel.CurrentDutyPlanViewModel

class CurrentDutyPlanViewModelFactory(
    private val planRepository: DutyPlanRepository,
    private val userRepository: UserRepository,
    private val placeRepository: DutyPlaceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CurrentDutyPlanViewModel(planRepository, userRepository, placeRepository) as T
    }
}
