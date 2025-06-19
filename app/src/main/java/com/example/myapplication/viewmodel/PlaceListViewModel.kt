package com.example.myapplication.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.DutyPlace
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.DutyPlaceRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.event.DataChangeEventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaceListViewModel(private val placeRepository: DutyPlaceRepository
) : ViewModel() {
    private val _places = MutableStateFlow<List<DutyPlace>>(emptyList())
    val places: StateFlow<List<DutyPlace>> = _places

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    init {
        loadPlaces()
    }

    fun loadPlaces() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _places.value = placeRepository.getDutyPlaces()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("PlaceListVM", "Error loading places", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun refresh() {
        loadPlaces()
    }
}

