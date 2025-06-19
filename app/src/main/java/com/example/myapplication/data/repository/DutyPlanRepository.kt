package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.api.RetrofitInstance
import com.example.myapplication.data.model.Duty
import com.example.myapplication.data.model.DutyChange
import com.example.myapplication.data.model.DutyPlanResponse
import com.example.myapplication.event.DataChangeEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class DutyPlanRepository(context: Context) {
    private val api = RetrofitInstance.getAuthenticatedApi(context)
    private val sharedPrefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun getCurrentAdminId(): Int {
        return sharedPrefs.getInt("admin_id", 0).takeIf { it != 0 }
            ?: throw IllegalStateException("Admin ID not found")
    }

    suspend fun getDutyPlans(): List<DutyPlanResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDutyPlans(getCurrentAdminId())
            if (response.isSuccessful) {
                response.body()?.let { plans ->
                    return@withContext plans
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

    suspend fun getCurrentDutyPlan(): DutyPlanResponse = withContext(Dispatchers.IO) {
        try {
            val response = api.getCurrentDutyPlan(getCurrentAdminId())
            if (response.isSuccessful) {
                response.body()?.let { plan ->
                    return@withContext plan
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

    suspend fun getDutiesForDutyPlan(dutyPlanId: Int): List<Duty> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDuties(dutyPlanId)
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Empty response body")
            } else {
                throw IOException("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("DutyScheduleRepo", "Error getting duties", e)
            throw e
        }
    }

    suspend fun saveDutyChanges(changes: List<DutyChange>): Boolean {
        return try {
            Log.d("Repository", "Saving ${changes.size} changes")
            val response = api.saveDutyChanges(changes)
            Log.d("Repository", "Response: ${response.code()}")
            response.isSuccessful
        } catch (e: SocketTimeoutException) {
            Log.e("Repository", "Timeout while saving changes", e)
            throw IOException("Таймаут соединения. Проверьте интернет")
        } catch (e: IOException) {
            Log.e("Repository", "Network error while saving changes", e)
            throw IOException("Ошибка сети. Проверьте подключение")
        } catch (e: Exception) {
            Log.e("Repository", "Unexpected error while saving changes", e)
            throw IOException("Неизвестная ошибка")
        }
    }

}