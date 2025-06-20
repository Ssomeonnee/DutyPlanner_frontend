package com.example.myapplication.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.DutyPlaceRepository
import com.example.myapplication.ui.viewmodel.PlaceListViewModel

class PlaceListViewModelFactory(
    private val placeRepository: DutyPlaceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlaceListViewModel(placeRepository) as T
    }
}