package com.kabos.spotifydj.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kabos.spotifydj.util.OneShotEvent
import com.kabos.spotifydj.util.Pager
import com.kabos.spotifydj.util.ReplaceFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(): ViewModel() {

    private val _pagerPosition = MutableLiveData<OneShotEvent<Pager>>()
    private val _isEditPlaylistFragment = MutableLiveData<OneShotEvent<Boolean>>()

    val pagerPosition: LiveData<OneShotEvent<Pager>>
        get() = _pagerPosition
    val isEditPlaylistFragment: LiveData<OneShotEvent<Boolean>>
        get() = _isEditPlaylistFragment


    fun setPagerPosition(pager: Pager) {
        _pagerPosition.postValue(OneShotEvent(pager))
    }

    fun setEditPlaylistFragment() {
        _isEditPlaylistFragment.postValue(OneShotEvent(true))
    }
}
