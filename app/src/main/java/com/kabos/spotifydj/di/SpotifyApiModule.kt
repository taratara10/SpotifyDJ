package com.kabos.spotifydj.di

import com.kabos.spotifydj.model.apiConstants.ApiConstants.Companion.ENDPOINT
import com.kabos.spotifydj.repository.SpotifyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SpotifyApiModule {
    @Singleton
    @Provides
    fun provideSpotifyApi(): SpotifyApi =
        Retrofit.Builder()
            .baseUrl(ENDPOINT)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SpotifyApi::class.java)
}
