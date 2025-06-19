package com.example.myapplication.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DutyPlace (
    val id: Int,
    val name: String,
    @SerializedName("short_name") val shortName: String,
    @SerializedName("admin") val adminId: Int
) : Parcelable
{
    fun getSpinnerName(): String = "$shortName - $name"

}
