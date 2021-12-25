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
import com.kabos.spotifydj.util.OneShotEvent
import com.kabos.spotifydj.util.Pager
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
    var localPlaylistId = ""
    var localPlaylistTitle = ""

    val searchTrackList = MutableLiveData<List<TrackInfo>?>()
    val upperTrackList  = MutableLiveData<List<TrackInfo>?>()
    val downerTrackList = MutableLiveData<List<TrackInfo>?>()
    val localPlaylist = MutableLiveData<List<TrackInfo>?>()
    val currentTrack = MutableLiveData<TrackInfo?>()

    private val _usersPlaylist = MutableLiveData<List<PlaylistItem>>()
    private val _userCreatedPlaylist = MutableLiveData<List<PlaylistItem>>()


    //Loading Flag
    val isLoadingSearchTrack = MutableLiveData(false)
    val isLoadingUpperTrack = MutableLiveData(false)
    val isLoadingDownerTrack = MutableLiveData(false)
    val isLoadingPlaylistTrack = MutableLiveData(false)

    //Navigate Flag
    private val _setRootFragmentPagerPosition = MutableLiveData<OneShotEvent<Pager>>()

    // これ消したい
    val isNavigateNewPlaylistFragment = MutableLiveData(false)
    val isNavigateExistingPlaylistFragment = MutableLiveData(false)

    //MainFragment#onStart()で呼び出して、activityへ通知
    private val _needRefreshAccessToken = MutableLiveData<OneShotEvent<Boolean>>()
    val startExternalSpotifyApp = MutableLiveData(false)

    val usersPlaylist: LiveData<List<PlaylistItem>>
        get() = _usersPlaylist
    val userCreatedPlaylist: LiveData<List<PlaylistItem>>
        get() = _userCreatedPlaylist
    val setRootFragmentPagerPosition: LiveData<OneShotEvent<Pager>>
        get() = _setRootFragmentPagerPosition
    val needRefreshAccessToken: LiveData<OneShotEvent<Boolean>>
        get() = _needRefreshAccessToken


    /**
     * callback
     * */
    // 共通のcallbackを各FragmentのTrackAdapterに渡して
    // todo これいかんでしょ
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
        val trackItemsList = getTracksByKeyword(keyword) ?: return@launch
        val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList)
        searchTrackList.postValue(trackInfoList)
        isLoadingSearchTrack.value = false
    }


    private suspend fun getTracksByKeyword(keyword: String): List<TrackItems>? = withContext(Dispatchers.IO){
        async {
            val result = repository.getTracksByKeyword(accessToken = mAccessToken, keyword= keyword)
            return@async when (result){
                is SpotifyApiResource.Success -> result.data
                is SpotifyApiResource.Error -> {
                    when (result.reason) {
                        is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                        is SpotifyApiErrorReason.NotFound,
                        is SpotifyApiErrorReason.ResponseError,
                        is SpotifyApiErrorReason.UnKnown -> {
                            isLoadingSearchTrack.postValue(false)
                            //todo display onFetchFailed textView or Toast
                        }
                    }
                    null
                }
                else -> listOf()
            }

        }
    }.await()

    private suspend fun getAudioFeaturesById(id: String): AudioFeature? = withContext(Dispatchers.IO) {
        async {
            val result = repository.getAudioFeaturesById(accessToken = mAccessToken, id = id)
            return@async when (result){
                is SpotifyApiResource.Success -> result.data
                is SpotifyApiResource.Error -> {
                    when (result.reason) {
                        is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                        is SpotifyApiErrorReason.NotFound,
                        is SpotifyApiErrorReason.ResponseError,
                        is SpotifyApiErrorReason.UnKnown -> {
                            isLoadingSearchTrack.postValue(false)
                            isLoadingDownerTrack.postValue(false)
                            isLoadingUpperTrack.postValue(false)
                            //todo display onFetchFailed textView or Toast
                        }
                    }
                    null
                }
                else -> null
            }
        }
    }.await()

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

    private suspend fun generateTrackInfoList(trackItems: List<TrackItems>): List<TrackInfo>? =
        withContext(Dispatchers.IO) {
            async {
                //生成したTrackInfoを入れる仮の箱
                val mergedTrackInfoList = mutableListOf<TrackInfo>()

                trackItems.map { trackItems ->
                    val audioFeature = getAudioFeaturesById(trackItems.id)
                    val trackInfo = mergeTrackItemAndAudioFeature(trackItems, audioFeature)
                    if (trackInfo != null) mergedTrackInfoList.add(trackInfo)
                }
                return@async mergedTrackInfoList
            }
        }.await()



    /**
     * Recommendの処理
     * */

    //すぐにupdateRecommendTrackでcurrentTrack使いたいので、postValue()ではなくsetValue()
    fun updateCurrentTrack(track: TrackInfo){
        navigateRootFragmentPagerPosition(Pager.Recommend)
        currentTrack.value = track
        updateRecommendTrack()
    }


    private fun updateRecommendTrack() = viewModelScope.launch{
        if (currentTrack.value == null) return@launch
        //fetch upperTrack
        launch {
            isLoadingUpperTrack.value = true
            val trackItemsList = getRecommendTracks(currentTrack.value!!, fetchUpperTrack = true) ?: return@launch
            val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList)
            upperTrackList.postValue(trackInfoList)
            isLoadingUpperTrack.value = false
        }
        //fetch downerTrack
        launch {
            isLoadingDownerTrack.value = true
            val trackItemsList = getRecommendTracks(currentTrack.value!!, fetchUpperTrack = false) ?: return@launch
            val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList)
            downerTrackList.postValue(trackInfoList)
            isLoadingDownerTrack.value = false
        }
    }

    private suspend fun getRecommendTracks(trackInfo: TrackInfo, fetchUpperTrack: Boolean): List<TrackItems>? =
        withContext(Dispatchers.IO){
            async {
                val result = repository.getRecommendTracks(
                    accessToken = mAccessToken,
                    trackInfo = trackInfo,
                    fetchUpperTrack = fetchUpperTrack)
                return@async when (result) {
                    is SpotifyApiResource.Success -> result.data
                    is SpotifyApiResource.Error -> {
                        when (result.reason){
                            is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                            is SpotifyApiErrorReason.NotFound,
                            is SpotifyApiErrorReason.ResponseError,
                            is SpotifyApiErrorReason.UnKnown -> {
                                isLoadingSearchTrack.postValue(false)
                                isLoadingDownerTrack.postValue(false)
                                isLoadingUpperTrack.postValue(false)
                                //todo display onFetchFailed textView or Toast
                            }
                        }
                        null
                    }
                    else -> null
                }
            }
        }.await()

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


    fun createPlaylist() = viewModelScope.launch {
        launch {
            when (val result = repository.createPlaylist(mAccessToken,mUserId,localPlaylistTitle)) {
                is SpotifyApiResource.Success -> {
                    localPlaylistId = result.data.toString()
                }
                is SpotifyApiResource.Error -> {
                    when (result.reason){
                        is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                        is SpotifyApiErrorReason.NotFound,
                        is SpotifyApiErrorReason.ResponseError,
                        is SpotifyApiErrorReason.UnKnown -> {
                            localPlaylistId = result.data.toString()
                            //todo Toast出したい
//                            Toast.makeText(this@UserViewModel,"fail",Toast.LENGTH_SHORT).
                        }
                    }
                }
            }
        }.join()

        //localPlaylistのTrackを新規作成したplaylistに追加 空なら何もしない
        if (localPlaylist.value == null) return@launch
        val requestBody = AddTracksBody(localPlaylist.value?.map { it.contextUri }!!)
        repository.addTracksToPlaylist(mAccessToken, localPlaylistId, requestBody)
    }

    //ExistingPlaylistのtitleを変更する
    fun updatePlaylistTitle(title: String) = viewModelScope.launch {
        if (title.isEmpty()) return@launch

        when (val result = repository.updatePlaylistTitle(mAccessToken,localPlaylistId,title)) {
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
    fun addTrackToLocalPlaylist(track: TrackInfo){
        val playlist = (localPlaylist.value ?: mutableListOf()) as MutableList<TrackInfo>
        playlist.add(track)
        localPlaylist.postValue(playlist)
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
        if (localPlaylistId == "") return@launch
        val requestBody = AddTracksBody(listOf(trackInfo.contextUri))
        repository.addTracksToPlaylist(mAccessToken, localPlaylistId, requestBody)
    }


    //onSwipe callback
    private fun removeTrackFromLocalPlaylist(position:Int){
        val playlist = localPlaylist.value as MutableList<TrackInfo>
        val removeTrack = playlist.removeAt(position)
        localPlaylist.postValue(playlist)

        //todo イイ感じに改修する
        //nobody
        deleteTracksFromPlaylist(removeTrack)
    }

    private fun deleteTracksFromPlaylist(trackInfo: TrackInfo) = viewModelScope.launch {
        if (localPlaylistId == "") return@launch
        val requestBody = DeleteTracksBody(listOf(DeleteTrack(trackInfo.contextUri)))
        repository.deleteTracksFromPlaylist(mAccessToken, localPlaylistId, requestBody)
        //todo Toast出したい
    }

    //onDrop callback
    private fun changeTrackPositionInLocalPlaylist(initialPosition:Int, finalPosition:Int){
        val playlist = localPlaylist.value as MutableList<TrackInfo>
        val item = playlist.removeAt(initialPosition)
        playlist.add(finalPosition, item)
        localPlaylist.postValue(playlist)

        reorderPlaylistsTracks(initialPosition, finalPosition)
    }

    private fun reorderPlaylistsTracks(initialPosition: Int, finalPosition: Int) = viewModelScope.launch{
        if (localPlaylistId == "") return@launch
        repository.reorderPlaylistsTracks(mAccessToken,localPlaylistId,initialPosition,finalPosition)

    }



    /**
     * Dialog Playlist
     * */


    fun loadPlaylistIntoSearchFragment(playlist: PlaylistItem) = viewModelScope.launch{
        //keywordに一致する検索結果がなければreturn
        isLoadingSearchTrack.value = true
        val trackItemsList = getTracksByPlaylistId(playlist.id) ?: return@launch
        val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList)
        searchTrackList.postValue(trackInfoList)
        isLoadingSearchTrack.value = false
    }

    fun loadPlaylistIntoPlaylistFragment(playlist: PlaylistItem) = viewModelScope.launch {
        isLoadingPlaylistTrack.value = true
        val trackItemsList = getTracksByPlaylistId(playlist.id) ?: return@launch
        val trackInfoList:List<TrackInfo>? = generateTrackInfoList(trackItemsList)
        localPlaylist.postValue(trackInfoList)
        isLoadingPlaylistTrack.value = false
        //ついでにviewModelのパラメーターも更新
        updatePlaylistTitleAndId(playlist)
    }
    val loadedPlaylistTitle = MutableLiveData<String>()

    private fun updatePlaylistTitleAndId(playlist: PlaylistItem){
        //プレイリストを編集した際に、postするのに必要
        localPlaylistId = playlist.id
        //プレイリストのEditTextを更新
        loadedPlaylistTitle.postValue(playlist.name)
    }

    private suspend fun getTracksByPlaylistId(playlistId: String)
        : List<TrackItems>? = withContext(Dispatchers.IO){
            async {
                val result = repository.getTracksByPlaylistId(mAccessToken,playlistId)
                return@async when (result) {
                    is SpotifyApiResource.Success -> result.data
                    is SpotifyApiResource.Error -> {
                        when (result.reason){
                            is SpotifyApiErrorReason.UnAuthorized -> refreshAccessToken()
                            is SpotifyApiErrorReason.NotFound,
                            is SpotifyApiErrorReason.ResponseError,
                            is SpotifyApiErrorReason.UnKnown -> {
                                isLoadingSearchTrack.postValue(false)
                                isLoadingDownerTrack.postValue(false)
                                isLoadingUpperTrack.postValue(false)
                                //todo display onFetchFailed textView or Toast
                            }
                        }
                        null
                    }
                    else -> null
                }
            }
        }.await()


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
//        replaceTrackToPlaybackTrack(trackInfo,downerTrackList)
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
