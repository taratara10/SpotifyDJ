package com.kabos.spotifydj.repository

import com.kabos.spotifydj.model.User
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class Repository @Inject constructor( private val userService: UserService) {


    suspend fun getUser(accessToken: String): Response<User> =
        userService.getUser("Bearer $accessToken")



}
