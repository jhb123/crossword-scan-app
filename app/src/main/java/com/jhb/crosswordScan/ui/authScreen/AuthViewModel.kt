package com.jhb.crosswordScan.ui.authScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhb.crosswordScan.data.SessionData
import com.jhb.crosswordScan.network.CrosswordApi
import com.jhb.crosswordScan.userData.UserData
import com.jhb.crosswordScan.userData.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.ConnectException

private const val TAG = "AuthViewModel"

class AuthViewModel(private val repository: UserRepository)
    : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState : StateFlow<AuthUiState> = _uiState

    fun login(username: String, password: String){
        Log.i(TAG,"Logging in")
        viewModelScope.launch {
//            repository.allUsers.collect{
            repository.getUser(username, password).collect {
//            repository.getUserById(0).collect{

                if (it != null) {
                    Log.i(TAG,"Valid User")
                    SessionData.writeUser(it)
                    SessionData.readUser()
//            _uiState.update {
//                it.copy(
//                    userName = user.userName,
//                    userPassword = user.password,
//                    userEmail = user.email
//                )
//            }
                }
            }
        }

    }

    fun setUserName(name: String){
        Log.i(TAG,"username $name")
        _uiState.update {
            it.copy(userName = name)
        }
    }

    fun setPassword(password: String){
        Log.i(TAG,"password $password")
        _uiState.update {
            it.copy(userPassword = password)
        }
    }

    fun register(){

        if(uiState.value.userName != null && uiState.value.userPassword != null) {
            val user = UserData(
                userName = uiState.value.userName!!,
                password = uiState.value.userPassword!!,
                email = "${uiState.value.userName!!}@crosswordtest.com"
            )
            viewModelScope.launch {
                repository.insert(user)
            }
        }

    }

    fun testApi(){
        viewModelScope.launch {
            try {
                val serverResponse = CrosswordApi.retrofitService.getHello()
                Log.i(TAG, serverResponse)
            }
            catch (e : ConnectException){
                Log.i(TAG, "unable to find server")
            }
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