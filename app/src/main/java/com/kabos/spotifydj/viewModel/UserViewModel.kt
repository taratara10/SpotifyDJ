package com.kabos.spotifydj.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.model.Playlist
import com.kabos.spotifydj.model.User
import com.kabos.spotifydj.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    fun getUser(accessToken: String):User? = runBlocking {
        val request = repository.getUser(accessToken)
        if (request.isSuccessful) {
            return@runBlocking request.body()
        }else {
            return@runBlocking null
        }
    }

    fun getPlaylist(accessToken: String): Playlist? = runBlocking {
        val request = repository.getPlaylist(accessToken)
        if (request.isSuccessful){
            val item = request.body()?.items?.get(0)
            Log.d("VIEWMODEL", "${request.body()}/$item")


        }
        return@runBlocking null
    }


    fun getRecentlyPlayed(accessToken: String): String = runBlocking {
        val request = repository.getRecentlyPlayed(accessToken)
        if (request.isSuccessful){
            val track = request.body()!!.items[0].track.name
            Log.d("VIEWMODEL", "$track / ${request.body()}")
            return@runBlocking track
        }else {
            return@runBlocking "No track"
        }
    }

    fun playback(accessToken: String) = runBlocking {
        try {
            repository.playback(accessToken)
            Log.d("PLAYBACK", "playback success!")
        }catch (e: Exception){
           e.stackTrace
           Log.d("PLAYBACK", "playback failed")
        }
    }

    fun getCurrentPlayback(accessToken: String) = runBlocking {
        val request = repository.getCurrentPlayback(accessToken)

            Log.d("CURRENTPLAYBACK","${request.body()}")

    }
}
