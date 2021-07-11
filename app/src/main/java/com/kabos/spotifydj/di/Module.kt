package com.kabos.spotifydj.di

import com.kabos.spotifydj.repository.Repository
import com.kabos.spotifydj.repository.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiModule {

    val ENDPOINT = "https://api.spotify.com/v1/"

    @Singleton
    @Provides
    fun provideUserService(): UserService =
        Retrofit.Builder()
            .baseUrl(ENDPOINT)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(UserService::class.java)

    @Singleton
    @Provides
    fun provideRepository(userService: UserService) =
        Repository(userService)
}
