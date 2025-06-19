package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DutyPlan (
    val id: Int,
    val month: Int,
    val year: Int,
    val status: String,
    @SerializedName("admin") val adminId: Int
) : Parcelable


