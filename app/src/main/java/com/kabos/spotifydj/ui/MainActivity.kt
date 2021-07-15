package com.kabos.spotifydj.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.kabos.spotifydj.R
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val CLIENT_ID = "d343c712f57f4f02ace00abddfec1bb6"
    private val REDIRECT_URI = "com.kabos.spotifydj://callback"
    private val SCOPE = arrayOf("user-read-recently-played","playlist-read-private","playlist-read-collaborative","user-modify-playback-state","user-read-playback-state")
    private val REQUEST_CODE: Int = 1337
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        authorizationSpotify()
    }

    private fun authorizationSpotify() {
        val request = AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
            .setScopes(SCOPE)
            .build()
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response: AuthorizationResponse = AuthorizationClient.getResponse(resultCode, data)

            when(response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    getSharedPreferences("SPOTIFY", 0).edit()
                        .putString("token", response.accessToken)
                        .apply()
                    Log.d("STARTING", "GOT AUTH TOKEN")
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
    override fun onSupportNavigateUp(): Boolean {
        //Fragmentのコールバックがあればそれを実行する
        if (onBackPressedDispatcher.hasEnabledCallbacks()) onBackPressedDispatcher.onBackPressed()
        return true
    }
}