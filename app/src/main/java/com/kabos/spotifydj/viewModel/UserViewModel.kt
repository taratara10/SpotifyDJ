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
    private val mUserId: String by lazy { initializeUserId() }
    var currentPlaylistId = ""
    val searchTrackList = MutableLiveData<List<TrackInfo>?>()
    val upperTrackList = MutableLiveData<List<TrackInfo>?>()
    val downerTrackList = MutableLiveData<List<TrackInfo>?>()
    val currentTrack = MutableLiveData<TrackInfo>()
    val currentPlaylist = MutableLiveData<List<TrackInfo>>()
    var usersAllPlaylists = MutableLiveData<List<PlaylistItem>>()




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

    val playlistCallback = object :PlaylistCallback {
        override fun onClick(playlistItem: PlaylistItem) {
            updatePlaylistItemByDialog(playlistItem.id)
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

    fun addTrackToCurrentPlaylist(track: TrackInfo){
        updateCurrentTrack(track)
        val playlist = (currentPlaylist.value ?: mutableListOf()) as MutableList<TrackInfo>
        playlist.add(track)
        currentPlaylist.postValue(playlist)
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
        //keywordに一致する検索結果がなければreturn
        val trackItemsList = getTracksByKeyword(keyword).await() ?: return@launch
        val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
        searchTrackList.postValue(trackInfoList)
    }


    private suspend fun getTracksByKeyword(keyword: String): Deferred<List<TrackItems>?> = withContext(Dispatchers.IO){
        async {
            return@async repository.getTracksByKeyword(mAccessToken,keyword)
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

    suspend fun getRecommendTracks(trackInfo: TrackInfo, fetchUpperTrack: Boolean):Deferred<List<TrackItems>?> = withContext(Dispatchers.IO){
        async {
            return@async repository.getRecommendTracks(mAccessToken,trackInfo,fetchUpperTrack)
        }
    }

    /**
     *  Playlist
     * */

    fun getUsersAllPlaylists() = viewModelScope.launch {
        val request = repository.getUsersAllPlaylist(mAccessToken)

        Log.d("getUserPlaylist","${request.body()}")
        if (request.isSuccessful){
            usersAllPlaylists.postValue(request.body()?.items)
        }else{
            Log.d("getUserPlaylist","getUserPlaylist failed")
        }
    }

    private fun initializeUserId(): String = runBlocking {
        repository.getUsersProfile(mAccessToken)?.id.toString()
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
