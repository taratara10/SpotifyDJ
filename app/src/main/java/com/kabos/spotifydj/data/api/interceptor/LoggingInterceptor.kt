package com.kabos.spotifydj.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class LoggingInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        Timber.d("--response ${response.body?.string()}")
        return chain.proceed(request)
    }
}
