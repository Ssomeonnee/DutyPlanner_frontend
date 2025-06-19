package com.example.myapplication.data.api

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val BASE_URL = "http://192.168.209.127:9000/"
    //private val BASE_URL = "http://192.168.81.127:8000/"

    // Базовый API для неаутентифицированных запросов (логин/регистрация)
    val unauthenticatedApi: RetrofitInterface by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitInterface::class.java)
    }

    // Аутентифицированный API с токеном
    fun getAuthenticatedApi(context: Context): RetrofitInterface {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(
                GsonBuilder().serializeNulls().create()
            ))
            .build()
            .create(RetrofitInterface::class.java)
    }
}

