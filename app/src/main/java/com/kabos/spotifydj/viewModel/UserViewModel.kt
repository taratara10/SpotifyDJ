package com.kabos.spotifydj.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.model.TrackInfo
import com.kabos.spotifydj.model.apiResult.SpotifyApiErrorReason
import com.kabos.spotifydj.model.apiResult.SpotifyApiResource
import com.kabos.spotifydj.model.feature.AudioFeature
import com.kabos.spotifydj.model.playback.Device
import com.kabos.spotifydj.model.playlist.PlaylistItem
import com.kabos.spotifydj.model.requestBody.AddTracksBody
import com.kabos.spotifydj.model.requestBody.DeleteTrack
import com.kabos.spotifydj.model.requestBody.DeleteTracksBody
import com.kabos.spotifydj.model.track.TrackItems
import com.kabos.spotifydj.repository.*
import com.kabos.spotifydj.ui.adapter.AdapterCallback
import com.kabos.spotifydj.ui.adapter.DragTrackCallback
import com.kabos.spotifydj.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repository: Repository): ViewModel() {
    private var mAccessToken = ""
    private var mDeviceId = ""
    private var mUserId = ""
    private var mUserName = ""
    private var editingPlaylistId = ""

    private val _searchTracks = MutableLiveData<List<TrackInfo>>()
    private val _upperTracks = MutableLiveData<List<TrackInfo>>()
    private val _downerTracks = MutableLiveData<List<TrackInfo>>()
    private val _currentTrack = MutableLiveData<TrackInfo>()
    private val _editingPlaylist = MutableLiveData<List<TrackInfo>>()
    private val _editingPlaylistTitle = MutableLiveData<String>()
    private val _usersPlaylist = MutableLiveData<List<PlaylistItem>>()
    private val _userCreatedPlaylist = MutableLiveData<List<PlaylistItem>>()
    //Loading Flag
    val isLoadingSearchTrack = MutableLiveData(false)
    val isLoadingUpperTrack = MutableLiveData(false)
    val isLoadingDownerTrack = MutableLiveData(false)
    val isLoadingPlaylistTrack = MutableLiveData(false)

    //Navigate Flag
    private val _setRootFragmentPagerPosition = MutableLiveData<OneShotEvent<Pager>>()
    private val _needRefreshAccessToken = MutableLiveData<OneShotEvent<Boolean>>()
    val startExternalSpotifyApp = MutableLiveData(false)

    // これ消したい
    val isNavigateNewPlaylistFragment = MutableLiveData(false)
    val isNavigateExistingPlaylistFragment = MutableLiveData(false)


    val usersPlaylist: LiveData<List<PlaylistItem>>
        get() = _usersPlaylist
    val userCreatedPlaylist: LiveData<List<PlaylistItem>>
        get() = _userCreatedPlaylist
    val setRootFragmentPagerPosition: LiveData<OneShotEvent<Pager>>
        get() = _setRootFragmentPagerPosition
    val needRefreshAccessToken: LiveData<OneShotEvent<Boolean>>
        get() = _needRefreshAccessToken
    val editingPlaylist: LiveData<List<TrackInfo>>
        get() = _editingPlaylist
    val editingPlaylistTitle: LiveData<String>
        get() = _editingPlaylistTitle
    val searchTracks: LiveData<List<TrackInfo>>
        get() = _searchTracks
    val upperTracks: LiveData<List<TrackInfo>>
        get() = _upperTracks
    val downerTracks: LiveData<List<TrackInfo>>
        get() = _downerTracks
    val currentTrack: LiveData<TrackInfo>
        get() = _currentTrack

    /**
     * callback
     * */
    // 共通のcallbackを各FragmentのTrackAdapterに渡して
    // todo これいかんでしょ
    val callback = object: AdapterCallback {
        override fun addTrack(trackInfo: TrackInfo) {
            addTrackToEditingPlaylist(trackInfo)
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

    //todo deviceIdをSharePrefから取り出して初期化する
    fun initializeDeviceId(deviceId: String){
        mDeviceId = deviceId
    }

    /**
     * Util
     * */
    fun initializeAccessToken(accessToken: String) {
        mAccessToken = accessToken
        initUserAccount()
    }

    private fun initUserAccount() = viewModelScope.launch {
        when (val result = repository.getUsersProfile(mAccessToken)) {
            is SpotifyApiResource.Success -> {
                val user = result.data
                if (user != null) {
                    mUserId = user.id
                    mUserName = user.display_name
                }
                getAllPlaylists()
            }
            is SpotifyApiResource.Error -> {
                when (result.reason) {
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    is SpotifyApiErrorReason.NotFound,
                    is SpotifyApiErrorReason.ResponseError,
                    is SpotifyApiErrorReason.UnKnown -> {
                        Timber.d("${result.reason}")
                    }
                }
            }
        }
    }


    /**
     * SearchFragmentの処理
     * */
    fun updateSearchedTracksResult(keyword: String) = viewModelScope.launch {
        isLoadingSearchTrack.value = true
        when (val result = repository.searchTrackInfo(mAccessToken, keyword)) {
            is SpotifyApiResource.Success -> {
                _searchTracks.postValue(result.data ?: listOf())
            }
            is SpotifyApiResource.Error -> {
                when (result.reason) {
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    is SpotifyApiErrorReason.NotFound,
                    is SpotifyApiErrorReason.ResponseError,
                    is SpotifyApiErrorReason.UnKnown -> {
                        //todo display onFetchFailed textView or Toast
                    }
                }
            }
        }
        isLoadingSearchTrack.postValue(false)
    }

    /**
     * Recommendの処理
     * */

    //すぐにupdateRecommendTrackでcurrentTrack使いたいので、postValue()ではなくsetValue()
    fun updateCurrentTrack(track: TrackInfo){
        navigateRootFragmentPagerPosition(Pager.Recommend)
        _currentTrack.postValue(track)
        updateUpperRecommendTrack(track)
        updateDownerRecommendTrack(track)
    }

    private fun updateUpperRecommendTrack(trackInfo: TrackInfo) = viewModelScope.launch {
        isLoadingUpperTrack.value = true
        when (val result = repository.getRecommendTrackInfos(mAccessToken, trackInfo, true)) {
            is SpotifyApiResource.Success -> {
                _upperTracks.postValue(result.data ?: listOf())
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else  -> {
                        //error handle
                    }
                }
            }
        }
        isLoadingUpperTrack.value = false
    }

    private fun updateDownerRecommendTrack(trackInfo: TrackInfo) = viewModelScope.launch {
        isLoadingDownerTrack.value = true
        when (val result = repository.getRecommendTrackInfos(mAccessToken, trackInfo, false)) {
            is SpotifyApiResource.Success -> {
                _downerTracks.postValue(result.data ?: listOf())
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else  -> {
                        //error handle
                    }
                }
            }
        }
        isLoadingDownerTrack.value = false
    }


    /**
     *  Playlist
     * */
    fun getAllPlaylists() = viewModelScope.launch {
        when (val result = repository.getUsersAllPlaylist(mAccessToken)) {
            is SpotifyApiResource.Success -> {
                val playlist = result.data ?: listOf()
                _usersPlaylist.postValue(playlist)
                filterOwnPlaylist(playlist)
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    is SpotifyApiErrorReason.NotFound,
                    is SpotifyApiErrorReason.ResponseError,
                    is SpotifyApiErrorReason.UnKnown -> {
                        //error handle
                    }
                }
            }
        }
    }
    private fun filterOwnPlaylist(playlist:List<PlaylistItem>) {
        // todo idでなくていいの？
        _userCreatedPlaylist.postValue(playlist.filter { it.owner.display_name == mUserName })
    }


    fun createPlaylist(title: String) = viewModelScope.launch {
        when (val result = repository.createPlaylist(mAccessToken, mUserId, title)) {
            is SpotifyApiResource.Success -> {
                editingPlaylistId = result.data.toString()
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    is SpotifyApiErrorReason.NotFound,
                    is SpotifyApiErrorReason.ResponseError,
                    is SpotifyApiErrorReason.UnKnown -> {
//                            Toast.makeText(this@UserViewModel,"fail",Toast.LENGTH_SHORT).
                    }
                }
            }
        }
        //todo createPlaylist時に、editingPalylistの内容をpostする処理
        //localPlaylistのTrackを新規作成したplaylistに追加 空なら何もしない
//        if (localPlaylist.value == null) return@launch
//        val requestBody = AddTracksBody(localPlaylist.value?.map { it.contextUri }!!)
//        repository.addTracksToPlaylist(mAccessToken, editingPlaylistId, requestBody)
    }

    //ExistingPlaylistのtitleを変更する
    fun updatePlaylistTitle(title: String) = viewModelScope.launch {
        if (title.isEmpty()) return@launch

        when (val result = repository.updatePlaylistTitle(mAccessToken, editingPlaylistId, title)) {
            is SpotifyApiResource.Success -> {
                //todo Toast(タイトルを更新しました)
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    is SpotifyApiErrorReason.NotFound,
                    is SpotifyApiErrorReason.ResponseError,
                    is SpotifyApiErrorReason.UnKnown -> {
                        //todo Toast出したい
//                            Toast.makeText(this@UserViewModel,"fail",Toast.LENGTH_SHORT).
                    }
                }
            }
        }
    }

    //onAdd callback
    fun addTrackToEditingPlaylist(track: TrackInfo){
        _editingPlaylist.addItem(track)

        updateCurrentTrack(track)
        postTracksToPlaylist(track)

        //New or Existing playlistに移動してないなら、playlistをnewPlaylistFragmentにreplace
        if (isNavigateNewPlaylistFragment.value!! || isNavigateExistingPlaylistFragment.value!!){
            navigateRootFragmentPagerPosition(Pager.Playlist)
        }else{
            isNavigateNewPlaylistFragment.postValue(true)
        }
    }






    //addItemToCurrentPlaylistと名前が似てるので、add -> postに変更した
    private fun postTracksToPlaylist(trackInfo: TrackInfo) = viewModelScope.launch {
        if (editingPlaylistId.isEmpty()) return@launch
        val requestBody = AddTracksBody(listOf(trackInfo.contextUri))
        repository.addTracksToPlaylist(mAccessToken, editingPlaylistId, requestBody)
    }


    //onSwipe callback
    private fun removeTrackFromLocalPlaylist(position:Int){
        val removeTrack = _editingPlaylist.removeAt(position)

        //todo deleteと処理をまとめたい
        if (removeTrack != null) deleteTracksFromPlaylist(removeTrack)
    }

    private fun deleteTracksFromPlaylist(trackInfo: TrackInfo) = viewModelScope.launch {
        if (editingPlaylistId.isEmpty()) return@launch
        // todo ここrepositoryに押し込む
        val requestBody = DeleteTracksBody(listOf(DeleteTrack(trackInfo.contextUri)))
        repository.deleteTracksFromPlaylist(mAccessToken, editingPlaylistId, requestBody)
        //todo Toast出したい
    }

    //onDrop callback
    private fun changeTrackPositionInLocalPlaylist(initialPosition:Int, finalPosition:Int){
        _editingPlaylist.replacePosition(initialPosition, finalPosition)
        reorderPlaylistsTracks(initialPosition, finalPosition)
    }

    private fun reorderPlaylistsTracks(initialPosition: Int, finalPosition: Int) = viewModelScope.launch{
        if (editingPlaylistId.isEmpty()) return@launch
        repository.reorderPlaylistsTracks(mAccessToken,editingPlaylistId,initialPosition,finalPosition)

    }



    /**
     * Dialog Playlist
     * */


    fun loadPlaylistIntoSearchFragment(playlist: PlaylistItem) = viewModelScope.launch{
        getTracksByPlaylistId(playlist.id, Pager.Search)
    }


    fun loadPlaylistIntoPlaylistFragment(playlist: PlaylistItem) = viewModelScope.launch {
        getTracksByPlaylistId(playlist.id, Pager.Playlist)
        updatePlaylistTitleAndId(playlist)
    }


    private fun getTracksByPlaylistId(playlistId: String, postFragment: Pager) = viewModelScope.launch {
        isLoadingPlaylistTrack.value = true
        when (val result = repository.getTrackInfosByPlaylistId(mAccessToken, playlistId)) {
            is SpotifyApiResource.Success -> {
                when (postFragment) {
                    is Pager.Search -> _searchTracks.postValue(result.data ?: listOf())
                    is Pager.Playlist -> _editingPlaylist.postValue(result.data ?: listOf())
                }
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    else  -> {
                        //error handle
                    }
                }
            }
        }
        isLoadingPlaylistTrack.value = false
    }

    private fun updatePlaylistTitleAndId(playlist: PlaylistItem){
        editingPlaylistId = playlist.id
        _editingPlaylistTitle.postValue(playlist.name)
    }

    fun clearSearchTracks() {
        _searchTracks.postValue(listOf())
    }

    fun updateEditingPlaylistTitle(title: String) {
        _editingPlaylistTitle.postValue(title)
    }

    /**
     * Playback
     * */

    private fun getUsersDevices() = viewModelScope.launch {
        when (val result = repository.getUsersDevices(mAccessToken)) {
            is SpotifyApiResource.Success -> {
                //sharedPrefに詰めて運用したかったけど、activeじゃないとdeviceId指定しても404
                //なので、毎回Spotifyアプリを開いて、deviceIdを取得
                val userDevice: Device? =result.data?.find { it.type == "Smartphone" }
                if (userDevice != null) {
                    mDeviceId = userDevice.id
                }else {
                    startExternalSpotifyApp.postValue(true)
                }
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    is SpotifyApiErrorReason.NotFound -> startExternalSpotifyApp.postValue(true)
                    is SpotifyApiErrorReason.ResponseError,
                    is SpotifyApiErrorReason.UnKnown -> {
                        //todo Toast出したい
//                            Toast.makeText(this@UserViewModel,"fail",Toast.LENGTH_SHORT).
                    }
                }
            }
        }


    }

    fun playbackTrack(trackInfo: TrackInfo) = viewModelScope.launch {
        if (mDeviceId.isEmpty()) getUsersDevices()

        when (val result = repository.playbackTrack(mAccessToken, mDeviceId, trackInfo.contextUri)) {
            is SpotifyApiResource.Success -> {
               //icon変えたりする？
            }
            is SpotifyApiResource.Error -> {
                when (result.reason){
                    is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                    is SpotifyApiErrorReason.NotFound -> getUsersDevices()
                    is SpotifyApiErrorReason.ResponseError,
                    is SpotifyApiErrorReason.UnKnown -> {
                        //todo Toast出したい
//                            Toast.makeText(this@UserViewModel,"fail",Toast.LENGTH_SHORT).
                    }
                }
            }
        }
    }
//        //isPlaybackによって、再生、停止を行う
//        if (trackInfo.isPlayback){
//            repository.pausePlayback(mAccessToken,mDeviceId)
//        }else {
//            repository.playbackTrack(mAccessToken, mDeviceId, trackInfo.contextUri)
//        }
//        togglePlaybackIcon(trackInfo)


//    //▶の再生アイコンを切り替える
//    private fun togglePlaybackIcon(trackInfo: TrackInfo){
//        replaceTrackToPlaybackTrack(trackInfo,searchTrackList)
//        replaceTrackToPlaybackTrack(trackInfo,upperTrackList)
//        replaceTrackToPlaybackTrack(trackInfo,_downerTracs)
//        replaceTrackToPlaybackTrack(trackInfo,localPlaylist as MutableLiveData<List<TrackInfo>?>)
//
//        //currentTrackはListじゃないので別処理
//        if(currentTrack.value == trackInfo){
//            trackInfo.isPlayback = !trackInfo.isPlayback
//            currentTrack.postValue(trackInfo)
//        }
//    }
//
//
//    private fun replaceTrackToPlaybackTrack(trackInfo: TrackInfo,trackList:MutableLiveData<List<TrackInfo>?>){
//        if(trackList.value == null) return
//
//        val list = trackList.value!!.toMutableList()
//
//        for (item in list){
//            //他の再生中アイコンをリセット
//            if (item.isPlayback) item.isPlayback = false
//            //再生したいTrackがあれば変更
//            if (item == trackInfo) item.isPlayback = true
//        }
//        trackList.value = list
//    }

    fun navigateRootFragmentPagerPosition(pager: Pager) {
        _setRootFragmentPagerPosition.postValue(OneShotEvent(pager))
    }

    fun refreshAccessToken() {
        _needRefreshAccessToken.postValue(OneShotEvent(true))
    }
}
