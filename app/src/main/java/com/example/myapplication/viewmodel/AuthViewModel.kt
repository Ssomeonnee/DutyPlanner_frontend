package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.AuthResponse
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    sealed class AuthState {
        object Loading : AuthState()
        data class Error(val message: String = "") : AuthState()
        data class Success(val isAdmin: Boolean) : AuthState()
    }

    private val repository = AuthRepository()
    private val _authState = MutableLiveData<AuthState?>()
    val authState: LiveData<AuthState?> = _authState

    fun login(login: String, password: String, isAdmin: Boolean) {
        if (login.isEmpty() || password.isEmpty()) {
            //_error.value ="Все поля долдны быть заполнены"
            _authState.value = AuthState.Error("Все поля долдны быть заполнены")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authData = if (isAdmin) {
                    repository.loginAdmin(login, password)
                } else {
                    repository.loginUser(login, password)
                }

                saveAuthData(authData)
                _authState.value = AuthState.Success(authData.is_admin)
            } catch (e: Exception) {
                //showError("Ошибка сети: ${e.message}")
                _authState.value = AuthState.Error(e.message.toString())
                //_error.value = e.message
                _authState.value = null // Сбрасываем состояние загрузки
            }
        }
    }

    fun registerAdmin(login: String, password: String, confirmPassword: String) {
        if (login.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            //_error.value ="Все поля долдны быть заполнены"
            _authState.value = AuthState.Error("Все поля долдны быть заполнены")
            return
        }

        if (password != confirmPassword) {
            //_error.value ="Пароли не совпадают"
            _authState.value = AuthState.Error("Пароли не совпадают")
            return
        }

        // Проверка длины пароля (например, от 6 до 30 символов)
        if (password.length < 6 || password.length > 30) {
            _authState.value = AuthState.Error("Пароль должен быть не менее 6 символов")
            return
        }

        // Проверка на английские символы в логине
        if (!login.matches(Regex("^[a-zA-Z0-9]+$"))) {
            _authState.value = AuthState.Error("Логин содержит английские буквы и цифры")
            return
        }

        // Проверка на английские символы в пароле (можно добавить спецсимволы)
        if (!password.matches(Regex("^[a-zA-Z0-9]+$"))) {
            _authState.value = AuthState.Error("Пароль содержит английские буквы и цифры")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authData = repository.registerAdmin(login, password)
                saveAuthData(authData)
                _authState.value = AuthState.Success(true)
            }catch (e: Exception) {
                //showError("Ошибка сети: ${e.message}")
                //_error.value = e.message ?: "Неизвестная ошибка"
                _authState.value = AuthState.Error(e.message ?: "Неизвестная ошибка")
                _authState.value = null // Сбрасываем состояние загрузки
            }
        }
    }

    private fun saveAuthData(authData: AuthResponse) {
        val context = getApplication<Application>().applicationContext
        context.getSharedPreferences("auth", Context.MODE_PRIVATE).
        edit().apply {
            putString("token", authData.token)
            putInt("user_id", authData.user_id)
            putBoolean("is_admin", authData.is_admin)
            putInt("admin_id", authData.admin_id)
            apply()
        }
    }
}

