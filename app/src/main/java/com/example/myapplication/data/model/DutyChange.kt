package com.example.myapplication.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DutyChange(
    @SerializedName("plan_id") val planId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("date") val date: Int,
    @SerializedName("old_place_code") val oldPlaceCode: String?,
    @SerializedName("new_place_code") val newPlaceCode: String?
) : Parcelable