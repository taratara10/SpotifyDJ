package com.kabos.spotifydj.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playlist.AddItemToPlaylistBody
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.track.TrackItems
import com.kabos.spotifydj.repository.Repository
import com.kabos.spotifydj.ui.adapter.AdapterCallback
import com.kabos.spotifydj.ui.adapter.PlaylistCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    var mAccessToken = ""
    private var mUserId = ""
    var currentPlaylistId = ""
    val searchTrackList = MutableLiveData<List<TrackInfo>?>()
    val upperTrackList = MutableLiveData<List<TrackInfo>?>()
    val downerTrackList = MutableLiveData<List<TrackInfo>?>()
    var usersAllPlaylists = MutableLiveData<List<PlaylistItem>>()
    val currentTrack = MutableLiveData<TrackInfo?>()
    val currentPlaylist = MutableLiveData<List<TrackInfo>>()

    //Loading Flag
    val isLoadingSearchTrack = MutableLiveData(false)
    val isLoadingUpperTrack = MutableLiveData(false)
    val isLoadingDownerTrack = MutableLiveData(false)

    //Navigate Flag
    val isNavigateRecommendFragment = MutableLiveData(false)
    val isNavigatePlaylistFragment = MutableLiveData(false)





    //共通のcallbackを各FragmentのTrackAdapterに渡して
    val callback = object: AdapterCallback {
        override fun addTrack(trackInfo: TrackInfo) {
            addTrackToCurrentPlaylist(trackInfo)
        }

        override fun playback(trackInfo: TrackInfo) {
            playbackTrack(trackInfo)
        }

        override fun onClick(trackInfo: TrackInfo) {
            updateCurrentTrack(trackInfo)
        }
    }

    /**
     * Util
     * */
    fun initializeAccessToken(accessToken: String){
        mAccessToken = accessToken
    }


    fun playbackTrack(trackInfo: TrackInfo){

    }

    /**
     * SearchFragmentの処理
     * */

    /**
     * 1. getTracksByKeyword: 基本情報となるTrackItemをlistで取得
     * 2.generateTrackInfo
     *      2-1. getAudioFeatureById: TrackItemのidを元にtempoとかを取得
     *      3-1. mergeTrackItemAndAudioFeatureで TrackInfoを生成
     * 3.対応するLiveDataにpost
     */

    fun updateSearchedTracksResult(keyword: String) = viewModelScope.launch{
        isLoadingSearchTrack.postValue(true)
        //keywordに一致する検索結果がなければreturn
        val trackItemsList = getTracksByKeyword(keyword).await() ?: return@launch
        val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
        searchTrackList.postValue(trackInfoList)
        isLoadingSearchTrack.postValue(false)
    }


    private suspend fun getTracksByKeyword(keyword: String): Deferred<List<TrackItems>?> = withContext(Dispatchers.IO){
        async {
            return@async repository.getTracksByKeyword(mAccessToken,keyword)
            //todo errorの時にLoadingProgress消す処理をコールバックで
        }
    }

    private suspend fun getAudioFeaturesById(id: String): Deferred<AudioFeature?> = withContext(Dispatchers.IO) {
        async {
            return@async repository.getAudioFeaturesById(mAccessToken,id)
        }
    }

    private fun mergeTrackItemAndAudioFeature(trackItems: TrackItems?, audioFeature: AudioFeature?): TrackInfo? {
        return if (trackItems != null && audioFeature != null) {
            TrackInfo(
                id = trackItems.id,
                uri= trackItems.uri,
                name = trackItems.name,
                artist = trackItems.artists[0].name,
                imageUrl = trackItems.album.images[0].url,
                tempo = audioFeature.tempo,
                danceability = audioFeature.danceability,
                energy = audioFeature.energy
            )
        } else null
    }

    private suspend fun generateTrackInfoList(trackItems: List<TrackItems>):Deferred<List<TrackInfo>?> = withContext(Dispatchers.IO) {
        async {
            //生成したTrackInfoを入れる仮の箱
            val mergedTrackInfoList = mutableListOf<TrackInfo>()

            trackItems.map { trackItems ->
                val audioFeature = getAudioFeaturesById(trackItems.id).await()
                val trackInfo = mergeTrackItemAndAudioFeature(trackItems, audioFeature)
                if (trackInfo != null) mergedTrackInfoList.add(trackInfo)
            }
            return@async mergedTrackInfoList
        }
    }



    /**
     * Recommendの処理
     * */

    //すぐにupdateRecommendTrackでcurrentTrack使いたいので、postValue()ではなくsetValue()
    fun updateCurrentTrack(track: TrackInfo){
        isNavigateRecommendFragment.postValue(true)
        currentTrack.value = track
        updateRecommendTrack()
    }


    private fun updateRecommendTrack() = viewModelScope.launch{
        if (currentTrack.value == null) return@launch
        //fetch upperTrack
        launch {
            isLoadingUpperTrack.postValue(true)
            val trackItemsList = getRecommendTracks(currentTrack.value!!, fetchUpperTrack = true).await() ?: return@launch
            val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
            isLoadingUpperTrack.postValue(false)
            upperTrackList.postValue(trackInfoList)
        }
        //fetch downerTrack
        launch {
            isLoadingDownerTrack.postValue(false)
            val trackItemsList = getRecommendTracks(currentTrack.value!!, fetchUpperTrack = false).await() ?: return@launch
            val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
            isLoadingDownerTrack.postValue(false)
            downerTrackList.postValue(trackInfoList)
        }
    }

    private suspend fun getRecommendTracks(trackInfo: TrackInfo, fetchUpperTrack: Boolean):Deferred<List<TrackItems>?> = withContext(Dispatchers.IO){
        async {
            return@async repository.getRecommendTracks(mAccessToken,trackInfo,fetchUpperTrack)
        }
    }

    /**
     *  Playlist
     * */
    fun addTrackToCurrentPlaylist(track: TrackInfo){
        val playlist = (currentPlaylist.value ?: mutableListOf()) as MutableList<TrackInfo>
        playlist.add(track)
        currentPlaylist.postValue(playlist)

        isNavigatePlaylistFragment.postValue(true)
        updateCurrentTrack(track)
    }

    fun getUsersAllPlaylists() = viewModelScope.launch {
        usersAllPlaylists.postValue(repository.getUsersAllPlaylist(mAccessToken))
    }

    private fun initializeUserId() = runBlocking {
        mUserId = repository.getUsersProfile(mAccessToken)?.id.toString()
    }

    fun createPlaylist(title: String) = viewModelScope.launch {
        if (mUserId == "") initializeUserId()
        currentPlaylistId = repository.createPlaylist(mAccessToken,mUserId,title)
    }

    //addItemToCurrentPlaylistと名前が似てるので、add -> postに変更した
    fun postItemToPlaylist() = viewModelScope.launch {
        if (currentPlaylist.value == null) return@launch
        val body = AddItemToPlaylistBody(currentPlaylist.value?.map { it.uri }!!)
        repository.addItemToPlaylist(mAccessToken, currentPlaylistId,body)

        //todo deleteで消去してからaddしないとアレ　ついでにDiffするとありがたい
    }

    /**
     * Dialog Playlist
     * */


    fun updatePlaylistItemByDialog(playlistId: String) = viewModelScope.launch{
        //keywordに一致する検索結果がなければreturn
        val trackItemsList =getPlaylistItemById(playlistId).await() ?: return@launch
        val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
        searchTrackList.postValue(trackInfoList)
    }


    private suspend fun getPlaylistItemById(playlistId: String)
        : Deferred<List<TrackItems>?> = withContext(Dispatchers.IO){
        async {
            repository.getPlaylistItemById(mAccessToken,playlistId)
        }
    }

}
