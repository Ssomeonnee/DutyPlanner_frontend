package com.example.myapplication.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.DutyPlan
import com.example.myapplication.data.model.DutyPlanResponse
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.DutyPlanRepository
import com.example.myapplication.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArchiveViewModel(private val dutyPlanRepository: DutyPlanRepository) : ViewModel() {

    private val _dutyPlans = MutableStateFlow<List<DutyPlanResponse>>(emptyList())
    val dutyPlans: StateFlow<List<DutyPlanResponse>> = _dutyPlans

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    init {
        loadDutyPlans()
    }

    fun loadDutyPlans() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _dutyPlans.value = dutyPlanRepository.getDutyPlans()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("DutyPlanListVM", "Error loading dutyPlans", e)
            } finally {
                _loading.value = false
            }
        }
    }

}











