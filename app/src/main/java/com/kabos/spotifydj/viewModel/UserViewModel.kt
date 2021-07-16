package com.kabos.spotifydj.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kabos.spotifydj.model.Playlist
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.model.User
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.track.TrackItems
import com.kabos.spotifydj.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repository: Repository): ViewModel() {

   private val accessToken = ""
    val searchTrackList: MutableLiveData<List<TrackItems>> = MutableLiveData()

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

    fun searchTracks(accessToken: String, keyword: String) = runBlocking{
        val request = repository.searchTracks(accessToken,keyword)

        if (request.isSuccessful){
            Log.d("SEARCH", "${request.body()}")
            val trackList = request.body()?.tracks?.items

        }else {
            Log.d("SEARCH","search failed")
        }
    }

    suspend fun getAudioFeatures(accessToken: String, id: String) = withContext(Dispatchers.IO) {
        async {
            val request = repository.getAudioFeaturesById(accessToken, id)
            if (request.isSuccessful) {
                return@async request.body() as AudioFeature
            }else{
                Log.d("getAudioFeature","getAudioFeature failed")
            }
        }
    }


    fun mergeTrackInfo(trackItems: TrackItems, audioFeature: AudioFeature): TrackInfo {
        return TrackInfo(
            id = trackItems.id,
            name = trackItems.name,
            artist = trackItems.artists[0].name,
            imageUrl = trackItems.album.images[0].url,
            tempo = audioFeature.tempo
        )

    }
}
