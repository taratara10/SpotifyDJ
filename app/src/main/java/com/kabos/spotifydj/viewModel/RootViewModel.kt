package com.kabos.spotifydj.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kabos.spotifydj.util.OneShotEvent
import com.kabos.spotifydj.util.Pager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(): ViewModel() {
    private val _pagerPosition = MutableLiveData<OneShotEvent<Pager>>()

    val pagerPosition: LiveData<OneShotEvent<Pager>>
        get() = _pagerPosition

    fun setPagerPosition(pager: Pager) {
        _pagerPosition.postValue(OneShotEvent(pager))
    }
}
