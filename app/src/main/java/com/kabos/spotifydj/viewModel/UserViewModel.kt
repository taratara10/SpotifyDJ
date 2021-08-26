package com.kabos.spotifydj.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.model.User
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playback.Device
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.requestBody.AddTracksBody
import com.kabos.spotifydj.model.requestBody.DeleteTrack
import com.kabos.spotifydj.model.requestBody.DeleteTracksBody
import com.kabos.spotifydj.model.track.TrackItems
import com.kabos.spotifydj.repository.Repository
import com.kabos.spotifydj.ui.adapter.AdapterCallback
import com.kabos.spotifydj.ui.adapter.DragTrackCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    var mAccessToken = ""
    var mDeviceId = ""
    var mUserId = ""
    var mUserName = ""

    var localPlaylistId = ""
    val searchTrackList = MutableLiveData<List<TrackInfo>?>()
    val upperTrackList  = MutableLiveData<List<TrackInfo>?>()
    val downerTrackList = MutableLiveData<List<TrackInfo>?>()
    val localPlaylist = MutableLiveData<List<TrackInfo>?>()
    val currentTrack = MutableLiveData<TrackInfo?>()

    val allPlaylists = MutableLiveData<List<PlaylistItem>?>()
    val filterOwnPlaylist = MutableLiveData<List<PlaylistItem>?>()


    //Loading Flag
    val isLoadingSearchTrack = MutableLiveData(false)
    val isLoadingUpperTrack = MutableLiveData(false)
    val isLoadingDownerTrack = MutableLiveData(false)

    //Navigate Flag
    val isNavigateSearchFragment = MutableLiveData(false)
    val isNavigateRecommendFragment = MutableLiveData(false)
    val isNavigatePlaylistFragment = MutableLiveData(false)



    /**
     * callback
     * */
    //共通のcallbackを各FragmentのTrackAdapterに渡して
    val callback = object: AdapterCallback {
        override fun addTrack(trackInfo: TrackInfo) {
            addTrackToLocalPlaylist(trackInfo)
        }

        override fun playback(trackInfo: TrackInfo) {
            playbackTrack(trackInfo)
        }

        override fun onClick(trackInfo: TrackInfo) {
            updateCurrentTrack(trackInfo)
        }
    }

    val dragTrackCallback = object :DragTrackCallback{
        override fun onClick(trackInfo: TrackInfo) {
            updateCurrentTrack(trackInfo)
        }

        override fun playback(trackInfo: TrackInfo) {
            playbackTrack(trackInfo)
        }

        override fun onSwiped(position: Int) {
            removeTrackFromLocalPlaylist(position)
        }

        override fun onDropped(initial: Int, final: Int) {
            changeTrackPositionInLocalPlaylist(initial,final)
        }

    }

    /**
     * Util
     * */
    fun initializeAccessToken(accessToken: String){
        mAccessToken = accessToken
    }

    private fun getUserProfile() = viewModelScope.launch {
        val userProfile:User? = repository.getUsersProfile(mAccessToken)
        if (userProfile != null){
            mUserId = userProfile.id
            mUserName = userProfile.display_name
        }
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
        isLoadingSearchTrack.value = true
        //keywordに一致する検索結果がなければreturn
        val trackItemsList = getTracksByKeyword(keyword).await() ?: return@launch
        val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
        searchTrackList.postValue(trackInfoList)
        isLoadingSearchTrack.value = false
    }


    private suspend fun getTracksByKeyword(keyword: String): Deferred<List<TrackItems>?> = withContext(Dispatchers.IO){
        async {
            return@async repository.getTracksByKeyword(
                accessToken = mAccessToken,
                keyword= keyword,
                onFetchFailed= {
                    isLoadingSearchTrack.postValue(false)
                    //todo display onFetchFailed textView
                })
        }
    }

    private suspend fun getAudioFeaturesById(id: String): Deferred<AudioFeature?> = withContext(Dispatchers.IO) {
        async {
            return@async repository.getAudioFeaturesById(
                accessToken = mAccessToken,
                id = id,
                onFetchFailed = {
                    isLoadingSearchTrack.postValue(false)
                    isLoadingDownerTrack.postValue(false)
                    isLoadingUpperTrack.postValue(false)
                })
        }
    }

    private fun mergeTrackItemAndAudioFeature(trackItems: TrackItems?, audioFeature: AudioFeature?): TrackInfo? {
        return if (trackItems != null && audioFeature != null) {
            TrackInfo(
                id = trackItems.id,
                contextUri = audioFeature.uri,
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
            isLoadingUpperTrack.value = true
            val trackItemsList = getRecommendTracks(currentTrack.value!!, fetchUpperTrack = true).await() ?: return@launch
            val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
            upperTrackList.postValue(trackInfoList)
            isLoadingUpperTrack.value = false
        }
        //fetch downerTrack
        launch {
            isLoadingDownerTrack.value = true
            val trackItemsList = getRecommendTracks(currentTrack.value!!, fetchUpperTrack = false).await() ?: return@launch
            val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
            downerTrackList.postValue(trackInfoList)
            isLoadingDownerTrack.value = false
        }
    }

    private suspend fun getRecommendTracks(trackInfo: TrackInfo, fetchUpperTrack: Boolean):Deferred<List<TrackItems>?> = withContext(Dispatchers.IO){
        async {
            return@async repository.getRecommendTracks(
                accessToken = mAccessToken,
                trackInfo = trackInfo,
                fetchUpperTrack = fetchUpperTrack,
                onFetchFailed = {
                    isLoadingSearchTrack.postValue(false)
                    isLoadingUpperTrack.postValue(false)
                    isLoadingDownerTrack.postValue(false)
                }
            )
        }
    }

    /**
     *  Playlist
     * */


    fun getAllPlaylists() = viewModelScope.launch {
        val playlist = repository.getUsersAllPlaylist(mAccessToken)
        if (playlist != null){
            allPlaylists.postValue(playlist)
            filterOwnPlaylist(playlist)
        }
    }
    private suspend fun filterOwnPlaylist(playlist:List<PlaylistItem>?) {
        if (mUserName == "") getUserProfile().join()
        filterOwnPlaylist.postValue(playlist?.filter { it.owner.display_name == mUserName })
    }


    fun createPlaylist(title: String) = viewModelScope.launch {
        //initialize userId
        if (mUserId == "") getUserProfile().join()
        launch {
            localPlaylistId = repository.createPlaylist(mAccessToken,mUserId,title)
        }.join()

        //localPlaylistのTrackを追加　空なら終了
        if (localPlaylist.value == null) return@launch
        val requestBody = AddTracksBody(localPlaylist.value?.map { it.contextUri }!!)
        repository.addTracksToPlaylist(mAccessToken, localPlaylistId, requestBody)
    }

    //addItemToCurrentPlaylistと名前が似てるので、add -> postに変更した
    private fun postTracksToPlaylist(trackInfo: TrackInfo) = viewModelScope.launch {
        if (localPlaylistId == "") return@launch
        val requestBody = AddTracksBody(listOf(trackInfo.contextUri))
        repository.addTracksToPlaylist(mAccessToken, localPlaylistId, requestBody)
    }

    private fun deleteTracksFromPlaylist(trackInfo: TrackInfo) = viewModelScope.launch {
        if (localPlaylistId == "") return@launch
        val requestBody = DeleteTracksBody(listOf(DeleteTrack(trackInfo.contextUri)))
        repository.deleteTracksFromPlaylist(mAccessToken, localPlaylistId, requestBody)
    }

    //onAdd callback
    fun addTrackToLocalPlaylist(track: TrackInfo){
        val playlist = (localPlaylist.value ?: mutableListOf()) as MutableList<TrackInfo>
        playlist.add(track)
        localPlaylist.postValue(playlist)

        isNavigatePlaylistFragment.postValue(true)
        updateCurrentTrack(track)
        postTracksToPlaylist(track)
    }
    //onSwipe callback
    private fun removeTrackFromLocalPlaylist(position:Int){
        val playlist = localPlaylist.value as MutableList<TrackInfo>
        val removeTrack = playlist.removeAt(position)
        localPlaylist.postValue(playlist)

        //todo イイ感じに改修する
        deleteTracksFromPlaylist(removeTrack)
    }

    //onDrop callback
    private fun changeTrackPositionInLocalPlaylist(initialPosition:Int, finalPosition:Int){
        val playlist = localPlaylist.value as MutableList<TrackInfo>
        val item = playlist.removeAt(initialPosition)
        playlist.add(finalPosition, item)
        localPlaylist.postValue(playlist)
    }



    /**
     * Dialog Playlist
     * */
    val loadedPlaylistTitle = MutableLiveData<String>()
    //editTextに読み込んだplaylist titleを表示する
    fun displayLoadedPlaylistTitle(title: String){
        loadedPlaylistTitle.postValue(title)
    }

    fun loadPlaylistIntoSearchFragment(playlistId: String) = viewModelScope.launch{
        //keywordに一致する検索結果がなければreturn
        isLoadingSearchTrack.value = true
        val trackItemsList = getTracksByPlaylistId(playlistId).await() ?: return@launch
        val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
        searchTrackList.postValue(trackInfoList)
        isLoadingSearchTrack.value = false
    }

    fun loadPlaylistIntoPlaylistFragment(playlistId: String) = viewModelScope.launch {
        val trackItemsList = getTracksByPlaylistId(playlistId).await() ?: return@launch
        val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList).await()
        localPlaylist.postValue(trackInfoList)
    }


    private suspend fun getTracksByPlaylistId(playlistId: String)
        : Deferred<List<TrackItems>?> = withContext(Dispatchers.IO){
        async {
            repository.getTracksByPlaylistId(mAccessToken,playlistId)
        }
    }


    /**
     * Playback
     * */

    private fun getUsersDevices() = viewModelScope.launch {
        val usersDevices:List<Device>? = repository.getUsersDevices(mAccessToken)
        Log.d("deviceId","$usersDevices")
        if (usersDevices != null){
//          mDeviceId = usersDevices.find { it.is_active }?.id.toString()
            mDeviceId = usersDevices.first().id
        }else{
            Log.d("fethUsersDevice","No active device. $usersDevices")
        }

        //isActiveを探す→無ければintentでSpotify開く
        //同時にcontext uriも送る　deviceIdなしで送れる...?
        //size == 0なら自動でそれ選択しよう
        //type == smartPhone > 1なら 「このアプリで再生dialog 」


        Log.d("currentPlayback","$usersDevices")
    }

    fun playbackTrack(trackInfo: TrackInfo) = viewModelScope.launch{
        if (mDeviceId == "") getUsersDevices()

        //isPlaybackによって、再生、停止を行う
        if (trackInfo.isPlayback){
            repository.pausePlayback(mAccessToken,mDeviceId)
        }else {
            repository.playbackTrack(mAccessToken, mDeviceId, trackInfo.contextUri)
        }
        togglePlaybackIcon(trackInfo)
    }


    //▶の再生アイコンを切り替える
    private fun togglePlaybackIcon(trackInfo: TrackInfo){
        replaceTrackToPlaybackTrack(trackInfo,searchTrackList)
        replaceTrackToPlaybackTrack(trackInfo,upperTrackList)
        replaceTrackToPlaybackTrack(trackInfo,downerTrackList)
        replaceTrackToPlaybackTrack(trackInfo,localPlaylist as MutableLiveData<List<TrackInfo>?>)

        //currentTrackはListじゃないので別処理
        if(currentTrack.value == trackInfo){
            trackInfo.isPlayback = !trackInfo.isPlayback
            currentTrack.postValue(trackInfo)
        }
    }


    private fun replaceTrackToPlaybackTrack(trackInfo: TrackInfo,trackList:MutableLiveData<List<TrackInfo>?>){
        if(trackList.value == null) return

        val list = trackList.value!!.toMutableList()

        for (item in list){
            //他の再生中アイコンをリセット
            if (item.isPlayback) item.isPlayback = false
            //再生したいTrackがあれば変更
            //todo stop/resumeの実装
            if (item == trackInfo) item.isPlayback = true
        }
        trackList.value = list

    }
}
