package com.kabos.spotifydj.di

import com.kabos.spotifydj.data.api.UserApi
import com.kabos.spotifydj.data.api.PlaylistApi
import com.kabos.spotifydj.data.api.TrackApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiModule {
    @Singleton
    @Provides
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)
    @Singleton
    @Provides
    fun provideTrackApi(retrofit: Retrofit): TrackApi =
        retrofit.create(TrackApi::class.java)
    @Singleton
    @Provides
    fun providePlaylistApi(retrofit: Retrofit): PlaylistApi =
        retrofit.create(PlaylistApi::class.java)
}
