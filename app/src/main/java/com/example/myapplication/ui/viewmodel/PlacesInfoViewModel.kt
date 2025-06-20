package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.DutyPlace
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.DutyPlaceRepository
import com.example.myapplication.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

class PlacesInfoViewModel (
    private val placeRepository: DutyPlaceRepository
) : ViewModel() {
    sealed class UiState {
        object Loading : UiState()
        object ViewMode : UiState()
        object EditMode : UiState()
        object CreateMode : UiState()
        object Success : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private var currentPLace: DutyPlace? = null

    fun initialize(place: DutyPlace?, initialMode: String) {
        currentPLace = place
        when {
            initialMode == "viewMode" -> _uiState.value = UiState.ViewMode
            initialMode == "editMode" -> _uiState.value = UiState.EditMode
            initialMode == "createMode" -> _uiState.value = UiState.CreateMode
        }
    }

    fun switchToEditMode() {
        currentPLace?.let {
            _uiState.value = UiState.EditMode
        }
    }

    fun saveDutyPlace(
        name: String,
        shortName: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                if (currentPLace == null) {
                    // Создание нового пользователя
                    DutyPlace(
                        id = 0,
                        name = name,
                        shortName = shortName,
                        adminId = placeRepository.getCurrentAdminId()
                    ).also {
                        placeRepository.createDutyPlace(it)
                        currentPLace = it
                    }
                } else {
                    // Обновление существующего
                    currentPLace!!.copy(
                        name = name,
                        shortName = shortName,
                        adminId = placeRepository.getCurrentAdminId()
                    ).also {
                        placeRepository.updateDutyPlace(it)
                        currentPLace = it
                    }
                }
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                val message = e.message
                //_uiState.value = UiState.Error(message)
                _toastMessage.emit(message.toString())
            }
        }
    }

    fun deleteDutyPlace() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                currentPLace?.id?.let { placeId ->
                    placeRepository.deleteDutyPlace(placeId)
                    _uiState.value = UiState.Success
                } ?: throw IllegalStateException("Place not selected")
            } catch (e: Exception) {
                _toastMessage.emit("Ошибка удаления: ${e.message}")
            }
        }
    }
}