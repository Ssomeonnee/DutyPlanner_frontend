package com.example.myapplication.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.DutyPlaceRepository
import com.example.myapplication.ui.viewmodel.PlacesInfoViewModel

class PlacesInfoViewModelFactory(
    private val placeRepository: DutyPlaceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlacesInfoViewModel(placeRepository) as T
    }
}