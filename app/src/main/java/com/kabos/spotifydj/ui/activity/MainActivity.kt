package com.kabos.spotifydj.ui.activity

import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.lifecycle.LiveData
import com.kabos.spotifydj.R
import com.kabos.spotifydj.databinding.ActivityMainBinding
import com.kabos.spotifydj.util.OneShotEvent
import com.kabos.spotifydj.ui.viewmodel.PlaylistViewModel
import com.kabos.spotifydj.ui.viewmodel.RecommendViewModel
import com.kabos.spotifydj.ui.viewmodel.SearchViewModel
import com.kabos.spotifydj.ui.viewmodel.UserViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        // Request code that will be used to verify if the result comes from correct activity
        // Can be any integer
        private const val REQUEST_CODE: Int = 1337
        private const val CLIENT_ID = "d343c712f57f4f02ace00abddfec1bb6"
        private const val REDIRECT_URI = "com.kabos.spotifydj://callback"
        private val SCOPE = arrayOf(
            "user-read-recently-played",
            "playlist-read-private",
            "playlist-read-collaborative",
            "user-modify-playback-state",
            "user-read-playback-state",
            "playlist-modify-public",
            "playlist-modify-private"
        )
    }

    private val userViewModel: UserViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private val recommendViewModel: RecommendViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()

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

    // todo startIntentも全部ここでobserveして、fragmentは通知送るだけにしたいかも
    private fun initViewModels() {
        userViewModel.startExternalSpotifyApp.observe(this){ startActivity->
            if (startActivity) startActivity(
                Intent().setComponent(
                    ComponentName(
                        "com.spotify.music",
                        "com.spotify.music.MainActivity")))
        }

        userViewModel.userAccount.observe(this) { user ->
            playlistViewModel.initUserAccount(user.id, user.display_name)
        }

        observeAccessTokenExpiration(userViewModel.needRefreshAccessToken)
        observeAccessTokenExpiration(searchViewModel.needRefreshAccessToken)
        observeAccessTokenExpiration(recommendViewModel.needRefreshAccessToken)
        observeAccessTokenExpiration(playlistViewModel.needRefreshAccessToken)
        observeToastMessage(userViewModel.toastMessageId)
        observeToastMessage(searchViewModel.toastMessageId)
        observeToastMessage(recommendViewModel.toastMessageId)
        observeToastMessage(playlistViewModel.toastMessageId)
    }

    private fun observeAccessTokenExpiration(liveData: LiveData<OneShotEvent<Boolean>>) {
        liveData.observe(this){ event ->
            event.getContentIfNotHandled()?.let { needRefresh ->
                if (needRefresh) authorizationSpotify()
            }
        }
    }

    private fun observeToastMessage(liveData: LiveData<Int>) {
        liveData.observe(this) { message ->
            Toast.makeText(this, getString(message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun initAccessTokenInViewModels(accessToken: String) {
        userViewModel.initUserAccount(accessToken)
        searchViewModel.initAccessToken(accessToken)
        recommendViewModel.initAccessToken(accessToken)
        playlistViewModel.initAccessToken(accessToken)
    }

}
