package com.example.myapplication.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DutyPlanResponse (
    val id: Int,
    val month: Int,
    val year: Int,
    val status: String,
    @SerializedName("admin_id") val adminId: Int,
    @SerializedName("assigned_users") val assignedUsers: List<User> = emptyList(),
    @SerializedName("duty_places") val dutyPlaces: List<DutyPlace> = emptyList(),
    val duties: List<Duty> = emptyList()
) : Parcelable
{
    fun getTitle(): String = getMonth() + " $year"

    fun getMonth(): String{
        when (month) {
            1 -> return "Январь"
            2 -> return "Февраль"
            3 -> return "Март"
            4 -> return "Апрель"
            5 -> return "Май"
            6 -> return "Июнь"
            7 -> return "Июль"
            8 -> return "Август"
            9 -> return "Сентябрь"
            10 -> return "Октябрь"
            11 -> return "Ноябрь"
            12 -> return "Декабрь"
        }
        return ""
    }
}