package com.kabos.spotifydj

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kabos.spotifydj.viewModel.UserViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity: AppCompatActivity() {
//
//    val userViewModel: UserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

    private val CLIENT_ID = "d343c712f57f4f02ace00abddfec1bb6"
    private val REDIRECT_URI = "com.kabos.spotifydj://callback"
    private val SCOPE = "user-read-recently-played"
    private val REQUEST_CODE: Int = 1337
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer 1337のがよい？

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requireNotNull(supportActionBar).hide()
        setContentView(R.layout.activity_splash)

        authorizationSpotify()

    }


    private fun authorizationSpotify() {
        val request = AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
            .setScopes(arrayOf(SCOPE))
            .build()
        Log.d("SPLASH", "called authentication")
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
}
