package com.example.myapplication.data.model

data class AuthResponse(
    val token: String,
    val user_id: Int,
    val is_admin: Boolean,
    val admin_id: Int
)