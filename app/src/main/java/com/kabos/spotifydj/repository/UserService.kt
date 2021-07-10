package com.kabos.spotifydj.repository

import android.content.SharedPreferences
import com.kabos.spotifydj.model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface UserService {
    @GET()
    suspend fun getUser(@Header("Authorization") accessToken: String):Response<User>
}
