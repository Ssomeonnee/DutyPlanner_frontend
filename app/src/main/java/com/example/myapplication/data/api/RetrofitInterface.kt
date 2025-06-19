package com.example.myapplication.data.api

import com.example.myapplication.data.model.AuthRequest
import com.example.myapplication.data.model.AuthResponse
import com.example.myapplication.data.model.Duty
import com.example.myapplication.data.model.DutyChange
import com.example.myapplication.data.model.DutyPlace
import com.example.myapplication.data.model.DutyPlan
import com.example.myapplication.data.model.DutyPlanResponse
import com.example.myapplication.data.model.RegisterRequest
import com.example.myapplication.data.model.User
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitInterface {

    @POST("api/admins/login/")
    suspend fun loginAdmin(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/users/login/")
    suspend fun loginUser(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/admins/register/")
    suspend fun registerAdmin(@Body request: RegisterRequest): Response<AuthResponse>


    @GET("api/dutyplaces/?active_only=true")
    suspend fun getDutyPlaces(@Query("admin_id") adminId: Int): Response<List<DutyPlace>>

    @POST("api/dutyplaces/")
    suspend fun createDutyPlace(@Body place: DutyPlace): Response<DutyPlace>

    @PUT("api/dutyplaces/{id}/")
    suspend fun updateDutyPlace(@Path("id") id: Int, @Body place: DutyPlace): Response<DutyPlace>

    @PUT("api/dutyplaces/{id}/archive/")
    suspend fun deleteDutyPlace(@Path("id") id: Int): Response<DutyPlace>


    @GET("api/dutyplans/?archived_only=true")
    suspend fun getDutyPlans(@Query("admin_id") adminId: Int): Response<List<DutyPlanResponse>>

    @POST("api/dutyplans/")
    suspend fun createDutyPlan(@Body plan: DutyPlan): Response<DutyPlan>

    @PUT("api/dutyplans/{id}/archive/")
    suspend fun deleteDutyPlan(@Path("id") id: Int): Response<DutyPlan>

    @GET("/api/dutyplans/get-current-month-plan/")
    suspend fun getCurrentDutyPlan(@Query("admin_id") id: Int): Response<DutyPlanResponse>


    @POST("api/dutyplans/save-changes/")
    suspend fun saveDutyChanges(
        //@Header("Authorization") token: String,
        @Body changes: List<DutyChange>
    ): Response<Unit>


    @GET("/api/users/?active_only=true")
    suspend fun getUsers(@Query("admin_id") adminId: Int): Response<List<User>>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<User>

    @POST("api/users/")
    suspend fun createUser(@Body user: User): Response<User>

    @PUT("api/users/{id}/")
    suspend fun updateUser(@Path("id") id: Int, @Body user: User): Response<User>

    @PUT("api/users/{id}/archive/")
    suspend fun deleteUser(@Path("id") id: Int): Response<User>


    @GET("api/duties/")
    suspend fun getDuties(@Query("duty_schedule_id") dutyPlanId: Int): Response<List<Duty>>

}

