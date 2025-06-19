package com.example.myapplication.data.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            .getString("token", null)

        return if (token != null) {
            chain.proceed(
                request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            )
        } else {
            chain.proceed(request)
        }
    }
}