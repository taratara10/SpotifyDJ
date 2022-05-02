package com.kabos.spotifydj.di

import com.kabos.spotifydj.data.api.UserApi
import com.kabos.spotifydj.data.api.PlaylistApi
import com.kabos.spotifydj.data.api.TrackApi
import com.kabos.spotifydj.data.repository.UserRepository
import com.kabos.spotifydj.data.repository.PlaylistRepository
import com.kabos.spotifydj.data.repository.TrackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Singleton
    @Provides
    fun provideUserRepository(userApi: UserApi): UserRepository =
        UserRepository(userApi)

    @Singleton
    @Provides
    fun provideTrackRepository(trackApi: TrackApi, playlistApi: PlaylistApi): TrackRepository =
        TrackRepository(trackApi, playlistApi)

    @Singleton
    @Provides
    fun providePlaylistRepository(playlistApi: PlaylistApi): PlaylistRepository =
        PlaylistRepository(playlistApi)
}
