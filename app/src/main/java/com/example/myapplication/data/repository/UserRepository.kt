package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.api.RetrofitInstance
import com.example.myapplication.data.model.User
import com.example.myapplication.event.DataChangeEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class UserRepository(context: Context) {
    private val api = RetrofitInstance.getAuthenticatedApi(context)
    private val sharedPrefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun getCurrentAdminId(): Int {
        return sharedPrefs.getInt("admin_id", 0).takeIf { it != 0 }
            ?: throw IllegalStateException("Admin ID not found")
    }

    suspend fun getUsers(): List<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUsers(getCurrentAdminId())
            if (response.isSuccessful) {
                response.body()?.let { users ->
                    Log.d("UserRepository", "Successfully fetched ${users.size} users")
                    return@withContext users
                } ?: throw IOException("Empty response body")
            } else {
                throw IOException("Server error: ${response.code()}")
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                401 -> "Ошибка авторизации. Проверьте логин и пароль"
                403 -> "Доступ запрещен. Недостаточно прав"
                404 -> "Данные не найдены. Проверьте параметры запроса"
                500 -> "Ошибка сервера. Попробуйте позже"
                502, 503, 504 -> "Сервер временно недоступен. Попробуйте позже"
                else -> "Ошибка сервера (код ${e.code()})"
            }
            Log.e("DutyPlacesRepository", "HTTP error: ${e.message}", e)
            throw IOException(errorMessage)
        } catch (e: IOException) {
            val errorMessage = when {
                e.message?.contains("failed to connect") == true ->
                    "Не удалось подключиться к серверу"
                e.message?.contains("timeout") == true ->
                    "Превышено время ожидания ответа от сервера"
                else -> "Ошибка сети. Проверьте подключение к интернету"
            }
            Log.e("DutyPlacesRepository", "Network error: ${e.message}", e)
            throw IOException(errorMessage)
        } catch (e: Exception) {
            Log.e("DutyPlacesRepository", "Unexpected error: ${e.message}", e)
            throw IOException("Неизвестная ошибка. Обратитесь в поддержку")
        }
    }

    suspend fun getUserById(id: Int): User = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserById(id)
            if (response.isSuccessful) {
                response.body()?.let { user ->
                    Log.d("UserRepository", "Fetched user: ${user.id}")
                    return@withContext user
                } ?: throw IOException("Empty response body")
            } else {
                throw IOException("Server error: ${response.code()}")
            }
        } catch (e: HttpException) {
            Log.e("UserRepository", "HTTP error getting user", e)
            throw IOException("Network error occurred")
        } catch (e: IOException) {
            Log.e("UserRepository", "Network error getting user", e)
            throw e
        }
    }

    suspend fun createUser(user: User): User = withContext(Dispatchers.IO) {
        try {
            val response = api.createUser(user)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("UserRepository", "Create user failed: $errorBody")

                DataChangeEventBus.notifyUserChanged()

                throw when {
                    response.code() == 400 -> {
                        try {
                            val errorJson = JSONObject(errorBody)
                            when (errorJson.optString("error_code")) {
                                "LOGIN_EXISTS" -> LoginAlreadyExistsException(errorJson.optString("message", "Логин уже занят"))
                                "INVALID_EMAIL" -> InvalidEmailException(errorJson.optString("message", "Некорректный формат email"))
                                else -> IOException(errorJson.optString("message", "Неверные данные"))
                            }
                        } catch (e: JSONException) {
                            IOException("Ошибка сервера: $errorBody")
                        }
                    }
                    response.code() == 401 -> IOException("Ошибка авторизации")
                    response.code() == 403 -> IOException("Доступ запрещен")
                    response.code() in 500..599 -> IOException("Ошибка сервера")
                    else -> IOException("Неизвестная ошибка: ${response.code()}")
                }
            }

            return@withContext response.body() ?: throw IOException("Пустой ответ сервера")

        } catch (e: SocketTimeoutException) {
            throw IOException("Таймаут соединения")
        } catch (e: LoginAlreadyExistsException) {
            throw IOException("Логин уже занят")
        } catch (e: InvalidEmailException) {
            throw IOException("Некорректный формат email")
        } catch (e: Exception) {
            throw IOException("Проверьте подключение к интернету")
        }
    }

    suspend fun updateUser(user: User): User = withContext(Dispatchers.IO) {
        try {
            val response = api.updateUser(user.id, user)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("UserRepository", "Create user failed: $errorBody")

                DataChangeEventBus.notifyUserChanged()

                throw when {
                    response.code() == 400 -> {
                        try {
                            val errorJson = JSONObject(errorBody)
                            when (errorJson.optString("error_code")) {
                                "LOGIN_EXISTS" -> LoginAlreadyExistsException(errorJson.optString("message", "Логин уже занят"))
                                else -> IOException(errorJson.optString("message", "Неверные данные"))
                            }
                        } catch (e: JSONException) {
                            IOException("Ошибка сервера: $errorBody")
                        }
                    }
                    response.code() == 401 -> IOException("Ошибка авторизации")
                    response.code() == 403 -> IOException("Доступ запрещен")
                    response.code() in 500..599 -> IOException("Ошибка сервера")
                    else -> IOException("Неизвестная ошибка: ${response.code()}")
                }
            }

            return@withContext response.body() ?: throw IOException("Пустой ответ сервера")

        } catch (e: SocketTimeoutException) {
            throw IOException("Таймаут соединения")
        } catch (e: LoginAlreadyExistsException) {
            throw IOException("Логин уже занят")
        } catch (e: Exception) {
            throw IOException("Проверьте подключение к интернету")
        }
    }

    class LoginAlreadyExistsException(message: String) : IOException(message)
    class InvalidEmailException(message: String) : IOException(message)

    suspend fun deleteUser(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Deleting user: $id")
            val response = api.deleteUser(id)

            DataChangeEventBus.notifyUserChanged()

            if (response.isSuccessful) {
                Log.d("UserRepository", "User deleted successfully")
                return@withContext true
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("UserRepository", "Delete user failed: $errorBody")
                throw IOException("Ошибка на сервере")
            }
        }  catch (e: IOException) {
            Log.e("UserRepository", "Network error deleting user", e)
            throw IOException("Проверьте подключение к интернету")
        }
    }
}

