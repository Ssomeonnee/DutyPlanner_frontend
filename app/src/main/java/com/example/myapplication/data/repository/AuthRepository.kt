package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.RetrofitInstance
import com.example.myapplication.data.model.AuthRequest
import com.example.myapplication.data.model.AuthResponse
import com.example.myapplication.data.model.RegisterRequest
import com.example.myapplication.data.repository.UserRepository.LoginAlreadyExistsException
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class AuthRepository() {

    private val api = RetrofitInstance.unauthenticatedApi

    suspend fun loginAdmin(login: String, password: String): AuthResponse {
        return handleAuthResponse { api.loginAdmin(AuthRequest(login, password)) }
    }

    suspend fun loginUser(login: String, password: String): AuthResponse {
        return handleAuthResponse { api.loginUser(AuthRequest(login, password)) }
    }

    suspend fun registerAdmin(login: String, password: String): AuthResponse {
        return handleAuthResponse {
            api.registerAdmin(RegisterRequest(login, password))
        }
    }

    private suspend fun <T> handleAuthResponse(apiCall: suspend () -> Response<T>): T {
        return try {
            val response = apiCall()

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("AuthRepository", "Auth request failed: $errorBody")

                throw when {
                    response.code() == 400 -> {
                        try {
                            val errorJson = JSONObject(errorBody)
                            when {
                                errorJson.has("error_code") && errorJson.getString("error_code") == "LOGIN_EXISTS" ->
                                    LoginAlreadyExistsException(errorJson.optString("message", "Логин уже занят"))
                                errorJson.has("error") ->
                                    AuthException(errorJson.getString("error"), response.code())
                                else ->
                                    AuthException("Неверные данные", response.code())
                            }
                        } catch (e: JSONException) {
                            AuthException("Ошибка сервера: $errorBody", response.code())
                        }
                    }
                    response.code() == 401 -> AuthException("Неверный логин или пароль", response.code())
                    response.code() == 403 -> AuthException("Доступ запрещен", response.code())
                    response.code() == 404 -> AuthException("Пользователь не найден", response.code())
                    response.code() in 500..599 -> AuthException("Ошибка сервера", response.code())
                    else -> AuthException("Неизвестная ошибка: ${response.code()}", response.code())
                }
            }

            response.body() ?: throw AuthException("Пустой ответ сервера", response.code())
        } catch (e: LoginAlreadyExistsException) {
            throw IOException("Логин уже занят")
        } catch (e: AuthException) {
            throw AuthException("Неверный логин или пароль", 408)
        } catch (e: SocketTimeoutException) {
            throw AuthException("Таймаут соединения", 408)
        } catch (e: IOException) {
            throw AuthException("Проверьте подключение к интернету", 0)
        } catch (e: Exception) {
            throw AuthException("Неизвестная ошибка: ${e.message}", 0)
        }
    }

    class LoginAlreadyExistsException(message: String) : AuthException(message, 400)

    open class AuthException(
        override val message: String,
        val statusCode: Int
    ) : IOException(message)

}




