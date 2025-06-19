package com.example.myapplication.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object DataChangeEventBus {
    private val _userChanges = MutableSharedFlow<Unit>()
    val userChanges: SharedFlow<Unit> = _userChanges

    private val _placeChanges = MutableSharedFlow<Unit>()
    val placeChanges: SharedFlow<Unit> = _placeChanges

    suspend fun notifyUserChanged() {
        _userChanges.emit(Unit)
    }

    suspend fun notifyPlaceChanged() {
        _placeChanges.emit(Unit)
    }
}