package com.kabos.spotifydj.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.fragment.app.activityViewModels
import com.kabos.spotifydj.R
import com.kabos.spotifydj.viewModel.UserViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val CLIENT_ID = "d343c712f57f4f02ace00abddfec1bb6"
    private val REDIRECT_URI = "com.kabos.spotifydj://callback"
    private val SCOPE = arrayOf("user-read-recently-played",
        "playlist-read-private",
        "playlist-read-collaborative",
        "user-modify-playback-state",
        "user-read-playback-state",
        "playlist-modify-public",
        "playlist-modify-private")
    private val REQUEST_CODE: Int = 1337
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer

    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        authorizationSpotify()
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO) // アプリ全体に適用
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
                    //以降accessTokenはviewModelのmAccessTokenを介して使う
                    viewModel.initializeAccessToken(response.accessToken)
                    //AccessTokenを取得できたらPlaylist Fragmentに表示する
                    viewModel.getAllPlaylists()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onSupportNavigateUp(): Boolean {
        //Fragmentのコールバックがあればそれを実行する
        if (onBackPressedDispatcher.hasEnabledCallbacks()) onBackPressedDispatcher.onBackPressed()
        return true
    }
}
