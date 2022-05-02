package com.kabos.spotifydj.data.api.interceptor

import android.content.Context
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants
import com.kabos.spotifydj.data.model.exception.TokenExpiredException
import com.kabos.spotifydj.ui.activity.MainActivity
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class AuthorizationInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(ApiConstants.AUTHORIZATION, "Bearer ${getAccessToken()}")
            .build()
        Timber.d("-- request $request")
        return chain.proceed(request)
    }

    private fun getAccessToken() : String {
        val activity = MainActivity.activity
        val preference = activity.getPreferences(Context.MODE_PRIVATE)
        val token = preference.getString(ApiConstants.AUTH_TOKEN, "")

        Timber.d("-- token ${preference.getString(ApiConstants.AUTH_TOKEN, "nulll")}")
        return if (!token.isNullOrEmpty()) token
        else throw TokenExpiredException()
    }
}
