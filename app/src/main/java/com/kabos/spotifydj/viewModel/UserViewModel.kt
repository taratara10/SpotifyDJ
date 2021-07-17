package com.kabos.spotifydj.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.model.Track
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.track.TrackItems
import com.kabos.spotifydj.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    var mAccessToken = ""
    val searchTrackList = MutableLiveData<List<TrackInfo>>()
    val upperTrackList = MutableLiveData<List<TrackInfo>>()
    val downerTrackList = MutableLiveData<List<TrackInfo>>()
    var currentTrack = MutableLiveData<TrackInfo>()

    fun initializeAccessToken(accessToken: String){
        mAccessToken = accessToken
    }

    /**
     * Util
     * */

    fun updateCurrentTrack(track: TrackInfo){
        currentTrack.postValue(track)
        //todo navigate to recommend fragment
    }

    /**
     * SearchFragmentの処理
     * */

    fun displaySearchedTracksResult(keyword: String) = viewModelScope.launch{

        //tempoとかの情報がないので、audioFeatureとmergeしてTrackInfoにする
        val trackItemsList = getTracksByKeyword(keyword).await() as List<TrackItems>
        Log.d("gKeyword","finished keyword")
        val trackInfoList = generateTrackInfoList(trackItemsList).await() as List<TrackInfo>

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

    private suspend fun generateTrackInfoList(trackItems: List<TrackItems>):Deferred<List<TrackInfo>?> = withContext(Dispatchers.IO) {
        async {
            val mergedTrackInfoList = mutableListOf<TrackInfo>()

            trackItems.map {
                val audioFeature = getAudioFeatures(it.id).await() as AudioFeature
                val mergedTrackInfo = mergeTrackInfo(it, audioFeature)
                mergedTrackInfoList.add(mergedTrackInfo)
            }
            return@async mergedTrackInfoList
        }
    }
    /**
     * Recommendの処理
     * */

    fun updateRecommendTrack() = viewModelScope.launch{
        if (currentTrack.value == null) return@launch

        //LiveDataにpostする用の箱
        val trackInfoList = mutableListOf<TrackInfo>()

        //tempoとかの情報がないので、audioFeatureとmergeしてTrackInfoにする
        val trackItemsList = getRecommendTracks(currentTrack.value!!.id).await() as List<TrackItems>
        trackItemsList.map {
            val audioFeature = getAudioFeatures(it.id).await() as AudioFeature
            val mergedTrackInfo = mergeTrackInfo(it,audioFeature)
            trackInfoList.add(mergedTrackInfo)
        }

        searchTrackList.postValue(trackInfoList)
    }

    suspend fun getRecommendTracks(seedTrackId: String) = withContext(Dispatchers.IO){
        async {
            val request = repository.getRecommendTracks(mAccessToken, seedTrackId)
            if (request.isSuccessful){
                return@async request.body()?.tracks
            }else{
                Log.d("getRecommendTracks","getRecommendTracks failed")
                return@async null
            }
        }
    }
}
