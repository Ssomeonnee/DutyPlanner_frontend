package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class UsersInfoViewModel(
    private val userRepository: UserRepository
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

    private var currentUser: User? = null

    fun initialize(user: User?, initialMode: String) {
        currentUser = user
        when {
            initialMode == "viewMode" -> _uiState.value = UiState.ViewMode
            initialMode == "editMode" -> _uiState.value = UiState.EditMode
            initialMode == "createMode" -> _uiState.value = UiState.CreateMode
        }
    }

    fun switchToEditMode() {
        currentUser?.let {
            _uiState.value = UiState.EditMode
        }
    }

    fun saveUser(
        surname: String,
        name: String,
        patronymic: String,
        email: String,
        login: String,
        password: String?
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                if (currentUser == null) {
                    // Создание нового пользователя

                    User(
                        id = 0,
                        surname = surname,
                        name = name,
                        patronymic = patronymic,
                        email = email,
                        login = login,
                        password = password.toString(),
                        adminId = userRepository.getCurrentAdminId()
                    ).also {
                        userRepository.createUser(it)
                        currentUser = it
                    }
                } else {

                    if (password == null) {
                        // Обновление существующего
                        currentUser!!.copy(
                            surname = surname,
                            name = name,
                            patronymic = patronymic,
                            email = email,
                            login = login,
                            adminId = userRepository.getCurrentAdminId()
                        ).also {
                            userRepository.updateUser(it)
                            currentUser = it
                        }
                    } else {
                        // Обновление существующего
                        currentUser!!.copy(
                            surname = surname,
                            name = name,
                            patronymic = patronymic,
                            email = email,
                            login = login,
                            password = password,
                            adminId = userRepository.getCurrentAdminId()
                        ).also {
                            userRepository.updateUser(it)
                            currentUser = it
                        }
                    }

                }
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                // Просто передаем сообщение об ошибке как есть
                _toastMessage.emit(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                currentUser?.id?.let { userId ->
                    userRepository.deleteUser(userId)
                    _uiState.value = UiState.Success
                } ?: throw IllegalStateException("User not selected")
            } catch (e: Exception) {
                //_uiState.value = UiState.Error("Ошибка удаления: ${e.message}")
                _toastMessage.emit("Ошибка удаления: ${e.message}")
            }
        }
    }
}