package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int,
    val surname: String,
    val name: String,
    val patronymic: String,
    val email: String,
    val login: String,
    val password: String,
    @SerializedName("admin") val adminId: Int,
) : Parcelable
{
    fun getFullName(): String = "$surname $name $patronymic"

    fun getSurnameWithInitials(): String {
        return surname+" "+name[0].toUpperCase().toString()+" "+patronymic[0].toUpperCase().toString()
    }
}

