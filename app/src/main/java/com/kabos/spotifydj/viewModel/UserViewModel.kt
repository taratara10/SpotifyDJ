package com.kabos.spotifydj.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabos.spotifydj.model.User
import com.kabos.spotifydj.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor( private val repository: Repository): ViewModel() {

    fun getUser(accessToken: String):User? = runBlocking {
        val request = repository.getUser(accessToken)
        if (request.isSuccessful) {
            return@runBlocking request.body()
        }else {
            return@runBlocking null
        }
    }
}
