package com.kabos.spotifydj.ui.activity

import android.content.ComponentName
import android.content.Context
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
import com.kabos.spotifydj.data.model.apiConstants.ApiConstants
import com.kabos.spotifydj.databinding.ActivityMainBinding
import com.kabos.spotifydj.ui.viewmodel.*
import com.kabos.spotifydj.util.OneShotEvent
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
        private const val REQUEST_CODE: Int = 2022
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

        lateinit var activity: MainActivity
    }

    private val userViewModel: UserViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private val recommendViewModel: RecommendViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val editingPlaylistViewModel: EditingPlaylistViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        activity = this

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO) // ????????????????????????
        initViewModels()
        launchAuthenticationActivity()
        userViewModel.getUserAccount()
    }

    private fun launchAuthenticationActivity() {
        val request = AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
            .setScopes(SCOPE)
            .build()
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    private fun saveAccessTokenInSharedPref(token: String) {
        val preference = getPreferences(Context.MODE_PRIVATE)
        with(preference.edit()) {
            putString(ApiConstants.AUTH_TOKEN, token)
            apply()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            val response: AuthorizationResponse = AuthorizationClient.getResponse(resultCode, data)
            when(response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    saveAccessTokenInSharedPref(response.accessToken)
                    Timber.d("GOT AUTH TOKEN")
                }
                else -> {
                    //???????????????????????????????????????????????????????????????
                    launchAuthenticationActivity()
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

    // todo startIntent??????????????????observe?????????fragment???????????????????????????????????????
    private fun initViewModels() {
        userViewModel.startExternalSpotifyApp.observe(this){ startActivity->
            if (startActivity) startActivity(
                Intent().setComponent(
                    ComponentName(
                        "com.spotify.music",
                        "com.spotify.music.MainActivity")))
        }

        observeAccessTokenExpiration(userViewModel.needRefreshAccessToken)
        observeAccessTokenExpiration(searchViewModel.needRefreshAccessToken)
        observeAccessTokenExpiration(recommendViewModel.needRefreshAccessToken)
        observeAccessTokenExpiration(playlistViewModel.needRefreshAccessToken)
        observeAccessTokenExpiration(editingPlaylistViewModel.needRefreshAccessToken)
        observeToastMessage(userViewModel.toastMessageId)
        observeToastMessage(searchViewModel.toastMessageId)
        observeToastMessage(recommendViewModel.toastMessageId)
        observeToastMessage(playlistViewModel.toastMessageId)
        observeToastMessage(editingPlaylistViewModel.toastMessageId)
    }

    private fun observeAccessTokenExpiration(liveData: LiveData<OneShotEvent<Unit>>) {
        liveData.observe(this){ event ->
            event.getContentIfNotHandled()?.let {
                launchAuthenticationActivity()
            }
        }
    }

    private fun observeToastMessage(liveData: LiveData<Int>) {
        liveData.observe(this) { message ->
            Toast.makeText(this, getString(message), Toast.LENGTH_SHORT).show()
        }
    }

}
