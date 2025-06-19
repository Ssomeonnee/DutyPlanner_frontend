package com.example.myapplication.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.DutyPlanRepository
import com.example.myapplication.viewmodel.ArchiveViewModel

class ArchiveListViewModelFactory(
    private val planRepository: DutyPlanRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ArchiveViewModel(planRepository) as T
    }
}