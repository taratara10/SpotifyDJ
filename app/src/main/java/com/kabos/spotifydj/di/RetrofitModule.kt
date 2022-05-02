package com.kabos.spotifydj.di

import com.kabos.spotifydj.BuildConfig
import com.kabos.spotifydj.data.api.interceptor.AuthorizationInterceptor
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RetrofitModule {
    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshiConverterFactory: MoshiConverterFactory
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiConstants.ENDPOINT)
            .client(okHttpClient)
            .addConverterFactory(moshiConverterFactory)
            .build()

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor())

        if (BuildConfig.DEBUG) {
            val httpInterceptor = HttpLoggingInterceptor()
            httpInterceptor.level = HttpLoggingInterceptor.Level.BASIC
            client.addInterceptor(httpInterceptor)
        }

        return client.build()
    }

}
