package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.api.RetrofitInstance
import com.example.myapplication.data.model.DutyPlace
import com.example.myapplication.data.repository.UserRepository.InvalidEmailException
import com.example.myapplication.data.repository.UserRepository.LoginAlreadyExistsException
import com.example.myapplication.event.DataChangeEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class DutyPlaceRepository(context: Context) {
    private val api = RetrofitInstance.getAuthenticatedApi(context)
    private val sharedPrefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun getCurrentAdminId(): Int {
        return sharedPrefs.getInt("admin_id", 0).takeIf { it != 0 }
            ?: throw IllegalStateException("Admin ID not found")
    }

    suspend fun getDutyPlaces(): List<DutyPlace> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDutyPlaces(getCurrentAdminId())
            if (response.isSuccessful) {
                response.body()?.let { places ->
                    Log.d("DutyPlacesRepository", "Successfully fetched ${places.size} places")
                    return@withContext places
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

    suspend fun createDutyPlace(place: DutyPlace): DutyPlace = withContext(Dispatchers.IO) {
        try {
            val response = api.createDutyPlace(place)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("UserRepository", "Create user failed: $errorBody")

                DataChangeEventBus.notifyPlaceChanged()

                throw when {
                    response.code() == 400 -> {
                        try {
                            val errorJson = JSONObject(errorBody)
                            when (errorJson.optString("error_code")) {
                                "SHORT_NAME_EXISTS" -> ShortNameExistsException(errorJson.optString("message", "Место с таким сокращением уже существует"))
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
        } catch (e: ShortNameExistsException) {
            throw IOException("Место с таким коротким названием уже существует")
        } catch (e: Exception) {
            throw IOException("Проверьте подключение к интернету")
        }

    }

    suspend fun updateDutyPlace(place: DutyPlace): DutyPlace = withContext(Dispatchers.IO) {
        try {
            val response = api.updateDutyPlace(place.id, place)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("UserRepository", "Create user failed: $errorBody")

                DataChangeEventBus.notifyPlaceChanged()

                throw when {
                    response.code() == 400 -> {
                        try {
                            val errorJson = JSONObject(errorBody)
                            when (errorJson.optString("error_code")) {
                                "SHORT_NAME_EXISTS" -> ShortNameExistsException(errorJson.optString("message", "Место с таким сокращением уже существует"))
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
        } catch (e: ShortNameExistsException) {
            throw IOException("Место с таким коротким названием уже существует")
        } catch (e: Exception) {
            throw IOException("Проверьте подключение к интернету")
        }
    }

    suspend fun deleteDutyPlace(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("DutyPlaceRepository", "Deleting place: $id")
            val response = api.deleteDutyPlace(id)

            DataChangeEventBus.notifyPlaceChanged()

            if (response.isSuccessful) {
                Log.d("DutyPlaceRepository", "Place deleted successfully")
                return@withContext true
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("DutyPlaceRepository", "Delete place failed: $errorBody")
                throw IOException("Ошибка на сервере")
            }
        } catch (e: IOException) {
            Log.e("DutyPlaceRepository", "Network error deleting place", e)
            throw IOException("Проверьте подключение к интернету")
        }
    }

    class ShortNameExistsException(message: String) : IOException(message)
}