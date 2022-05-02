package com.kabos.spotifydj.di

import com.kabos.spotifydj.data.repository.Repository
import com.kabos.spotifydj.data.api.SpotifyApi
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
    fun provideRepository(spotifyApi: SpotifyApi) =
        Repository(spotifyApi)
}
