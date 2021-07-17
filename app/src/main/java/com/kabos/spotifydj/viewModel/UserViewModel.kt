package com.kabos.spotifydj.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    var mAccessToken = ""
    val searchTrackList: MutableLiveData<List<TrackInfo>> = MutableLiveData()

    fun initializeAccessToken(accessToken: String){
        mAccessToken = accessToken
    }

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

    fun displaySearchedTracksResult(keyword: String) = viewModelScope.launch{
        //LiveDataにpostする用の箱
        val trackInfoList = mutableListOf<TrackInfo>()

        //tempoとかの情報がないので、audioFeatureとmergeしてTrackInfoにする
        val trackItemsList:List<TrackItems> = getTracksByKeyword(keyword).await() as List<TrackItems>
        trackItemsList.map {
            val audioFeature = getAudioFeatures(it.id).await() as AudioFeature
            val mergedTrackInfo = mergeTrackInfo(it,audioFeature)
            trackInfoList.add(mergedTrackInfo)
        }

        searchTrackList.postValue(trackInfoList)
        Log.d("Coroutine","finished")
    }

    suspend fun getTracksByKeyword(keyword: String): Deferred<List<TrackItems>?> = withContext(Dispatchers.IO){
        async {
            val request = repository.getTracksByKeyword(mAccessToken, keyword)
            if (request.isSuccessful){
                return@async request.body()?.tracks?.items as List<TrackItems>
            }else {
                Log.d("getTracksByKeyword","search failed")
                return@async null
            }
        }
    }

    suspend fun getAudioFeatures(id: String): Deferred<AudioFeature?> = withContext(Dispatchers.IO) {
        async {
            val request = repository.getAudioFeaturesById(mAccessToken, id)
            if (request.isSuccessful) {
                return@async request.body()
            }else{
                Log.d("getAudioFeature","getAudioFeature failed")
                return@async null
            }
        }
    }

    private fun mergeTrackInfo(trackItems: TrackItems, audioFeature: AudioFeature): TrackInfo {
        return TrackInfo(
            id = trackItems.id,
            name = trackItems.name,
            artist = trackItems.artists[0].name,
            imageUrl = trackItems.album.images[0].url,
            tempo = audioFeature.tempo
        )

    }
}
