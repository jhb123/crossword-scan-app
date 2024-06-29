package com.jhb.crosswordScan.data

import com.jhb.crosswordScan.network.CrosswordApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

private const val TAG = "Session"

object Session {

    private val _sessionDataState = MutableStateFlow<SessionData?>(null)
    val sessionDataState: StateFlow<SessionData?> = _sessionDataState


    suspend fun logOut(){
        CrosswordApi.retrofitService.logOut()
        _sessionDataState.update { null }
    }

    fun updateSession(sessionData: SessionData?){
        if(sessionData!=null){
            _sessionDataState.update { sessionData }
        }
    }

}

data class SessionData(
    val username : String?,
)