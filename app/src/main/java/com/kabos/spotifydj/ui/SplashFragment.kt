package com.kabos.spotifydj.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class SplashFragment: Fragment() {

    private val CLIENT_ID = "d343c712f57f4f02ace00abddfec1bb6"
    private val REDIRECT_URI = "com.kabos.spotifydj://callback"
    private val SCOPE = "user-read-recently-played"
    private val REQUEST_CODE: Int = 1337
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    private fun authorizationSpotify() {
        val request = AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
            .setScopes(arrayOf(SCOPE))
            .build()
        AuthorizationClient.openLoginActivity(activity, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response: AuthorizationResponse = AuthorizationClient.getResponse(resultCode, data)

            when(response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    requireActivity().getSharedPreferences("SPOTIFY", 0).edit()
                        .putString("token", response.accessToken)
                        .apply()
                    Log.d("STARTING", "GOT AUTH TOKEN")
//                    startMainActivity()
                }

                AuthorizationResponse.Type.ERROR ->{
                    // Handle error response
                }

                // Most likely auth flow was cancelled
                else -> {
                    //TODO
                    Log.d("SPLASH", "Cannot login")
                }
            }
        }

    }

}
