package com.sumplier.app.data.api.apiservice

import com.sumplier.app.data.model.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApiService {
    @GET("Users/GetUserLogin")
    fun getUserLogin(
        @Query("Email") email: String,
        @Query("Password") password: String,
    ): Call<User>
}