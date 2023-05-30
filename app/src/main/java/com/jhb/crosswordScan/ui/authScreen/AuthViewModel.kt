package com.jhb.crosswordScan.ui.authScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhb.crosswordScan.data.SessionData
import com.jhb.crosswordScan.userData.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AuthViewModel(private val repository: UserRepository)
    : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState : StateFlow<AuthUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.allUsers.collect { userList ->
                _uiState.update {
                    it.copy(
                        users = userList
                    )
                }
            }
        }
    }

    fun setUser(id: Int){
        viewModelScope.launch {
            SessionData.setUser(id)
        }
    }

    fun getUser(){
        viewModelScope.launch{

        }
    }
}

class AuthViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}