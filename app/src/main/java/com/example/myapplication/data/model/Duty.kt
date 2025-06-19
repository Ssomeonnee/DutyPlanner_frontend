package com.example.myapplication.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Duty (
    val id: Int,
    val date: Int,
    val status: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("duty_place_id") val dutyPlaceId: Int,
    @SerializedName("duty_schedule_id") val dutyScheduleId: Int,
) : Parcelable