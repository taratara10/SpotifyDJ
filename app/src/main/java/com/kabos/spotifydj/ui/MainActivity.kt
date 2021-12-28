package com.kabos.spotifydj.ui

import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.ActivityMainBinding
import com.kabos.spotifydj.util.OneShotEvent
import com.kabos.spotifydj.viewModel.RecommendViewModel
import com.kabos.spotifydj.viewModel.SearchViewModel
import com.kabos.spotifydj.viewModel.UserViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

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

    private val userViewModel: UserViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private val recommendViewModel: RecommendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO) // アプリ全体に適用
        initViewModels()
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
                    initAccessTokenInViewModels(response.accessToken)
                    Timber.d("GOT AUTH TOKEN")
                }
                else -> {
                    //再帰呼び出しでログインできるまでループする
                    authorizationSpotify()
                    Toast.makeText(this, getString(R.string.toast_login_failer), Toast.LENGTH_SHORT)
                        .show()
                    Timber.tag("SPLASH").d("Cannot login")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun initViewModels() {
        userViewModel.startExternalSpotifyApp.observe(this@MainActivity){ startActivity->
            if (startActivity) startActivity(
                Intent().setComponent(
                    ComponentName(
                        "com.spotify.music",
                        "com.spotify.music.MainActivity")))
        }

        observeAccessTokenExpiration(userViewModel.needRefreshAccessToken)
        observeAccessTokenExpiration(searchViewModel.needRefreshAccessToken)
        observeAccessTokenExpiration(recommendViewModel.needRefreshAccessToken)
    }

    private fun observeAccessTokenExpiration(liveData: LiveData<OneShotEvent<Boolean>>) {
        liveData.observe(this@MainActivity){ event ->
            event.getContentIfNotHandled()?.let { needRefresh ->
                if (needRefresh) authorizationSpotify()
            }
        }
    }

    private fun initAccessTokenInViewModels(accessToken: String) {
        userViewModel.initializeAccessToken(accessToken)
        searchViewModel.initAccessToken(accessToken)
        recommendViewModel.initAccessToken(accessToken)
    }
}
