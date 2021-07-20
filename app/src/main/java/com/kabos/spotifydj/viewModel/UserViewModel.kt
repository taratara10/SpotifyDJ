package com.kabos.spotifydj.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.track.TrackItems
import com.kabos.spotifydj.repository.Repository
import com.kabos.spotifydj.ui.adapter.AdapterCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    var mAccessToken = ""
    val searchTrackList = MutableLiveData<List<TrackInfo>?>()
    val upperTrackList = MutableLiveData<List<TrackInfo>?>()
    val downerTrackList = MutableLiveData<List<TrackInfo>?>()
    val currentTrack = MutableLiveData<TrackInfo>()
    val currentPlaylist = MutableLiveData<List<TrackInfo>>()




    val callback = object: AdapterCallback {
        override fun addTrack(trackInfo: TrackInfo) {
            addTrackToPlaylist(trackInfo)
        }

        override fun playback(trackInfo: TrackInfo) {
            playbackTrack(trackInfo)
        }

        override fun onClick(trackInfo: TrackInfo) {
            updateCurrentTrack(trackInfo)
        }
    }

    fun initializeAccessToken(accessToken: String){
        mAccessToken = accessToken
    }


    /**
     * Util
     * */

    fun updateCurrentTrack(track: TrackInfo){
        currentTrack.postValue(track)
        updateRecommendTrack()
    }

    fun addTrackToPlaylist(track: TrackInfo){
        updateCurrentTrack(track)
        val playlist: MutableList<TrackInfo> = (currentPlaylist.value ?: mutableListOf()) as MutableList<TrackInfo>
        playlist.add(track)
        currentPlaylist.postValue(playlist)
    }

    fun playbackTrack(trackInfo: TrackInfo){

    }

    /**
     * SearchFragmentの処理
     * */

    fun updateSearchedTracksResult(keyword: String) = viewModelScope.launch{
        //TrackItemを取得して、idからfeature(tempoとか)を取得して結合→TrackInfo
        val trackItemsList = getTracksByKeyword(keyword).await() ?: return@launch
        val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
        searchTrackList.postValue(trackInfoList)
    }

    private suspend fun getTracksByKeyword(keyword: String): Deferred<List<TrackItems>?> = withContext(Dispatchers.IO){
        async {
            val request = repository.getTracksByKeyword(mAccessToken, keyword)
            if (request.isSuccessful){
                return@async request.body()?.tracks?.items as List<TrackItems>
            }else {
                //todo check accessToken is enabled! and toast "need accestoken"
                Log.d("getTracksByKeyword","search failed")
                return@async null
            }
        }
    }

    private suspend fun getAudioFeaturesById(id: String): Deferred<AudioFeature?> = withContext(Dispatchers.IO) {
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

    private suspend fun generateTrackInfoList(trackItems: List<TrackItems>):Deferred<List<TrackInfo>?> = withContext(Dispatchers.IO) {
        async {
            val mergedTrackInfoList = mutableListOf<TrackInfo>()

            trackItems.map { trackItems ->
                val audioFeature = getAudioFeaturesById(trackItems.id).await()
                val trackInfo = mergeTrackItemAndAudioFeature(trackItems, audioFeature)
                if (trackInfo != null) {
                    mergedTrackInfoList.add(trackInfo)
                }
            }
            return@async mergedTrackInfoList
        }
    }

    private fun mergeTrackItemAndAudioFeature(trackItems: TrackItems?, audioFeature: AudioFeature?): TrackInfo? {
        return if (trackItems != null && audioFeature != null) {
            TrackInfo(
                id = trackItems.id,
                name = trackItems.name,
                artist = trackItems.artists[0].name,
                imageUrl = trackItems.album.images[0].url,
                tempo = audioFeature.tempo,
                danceability = audioFeature.danceability,
                energy = audioFeature.energy
            )
        } else null
    }
    /**
     * Recommendの処理
     * */

    fun updateRecommendTrack() = viewModelScope.launch{
        if (currentTrack.value == null) return@launch
        //fetch upperTrack
        launch {
            val trackItemsList = getRecommendTracks(currentTrack.value!!, fetchUpperTrack = true).await() ?: return@launch
            val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
            upperTrackList.postValue(trackInfoList)
        }
        //fetch downerTrack
        launch {
            val trackItemsList = getRecommendTracks(currentTrack.value!!, fetchUpperTrack = false).await() ?: return@launch
            val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
            downerTrackList.postValue(trackInfoList)
        }
    }

    suspend fun getRecommendTracks(trackInfo: TrackInfo, fetchUpperTrack: Boolean) = withContext(Dispatchers.IO){
        async {
            val request = repository.getRecommendTracks(mAccessToken, trackInfo, fetchUpperTrack)
            if (request.isSuccessful){
                Log.d("getRecommendTracks","success! ${request.body()?.tracks}")
                return@async request.body()?.tracks
            }else{
                Log.d("getRecommendTracks","getRecommendTracks failed")
                return@async null
            }
        }
    }

    /**
     *  Playlist
     * */

    fun getUsersPlaylists() = viewModelScope.launch {
        val request = repository.getUsersPlaylist(mAccessToken)

        Log.d("getUserPlaylist","${request.body()}")
        if (request.isSuccessful){
            Log.d("getUserPlaylist","${request.body()}")
        }else{
            Log.d("getUserPlaylist","getUserPlaylist failed")
        }
    }




}
