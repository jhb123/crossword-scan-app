package com.jhb.crosswordScan.ui.registerScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhb.crosswordScan.network.CrosswordApi
import com.jhb.crosswordScan.userData.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.ConnectException

private const val TAG = "RegistrationViewModel"

class RegistrationViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState : StateFlow<RegistrationUiState> = _uiState

    fun setUserName (username: String) {
        _uiState.update {
            it.copy(username = username)
        }
    }

    fun setPassword (password: String) {
        var errorMsg = ""
        var isError = false
        val isFilled = password != ""

        if (password != uiState.value.passwordConfirm){
            errorMsg = "passwords do not match"
            isError = true
        }

        _uiState.update {
            it.copy(
                password = password,
                message = errorMsg,
                errorState = isError,
                filledPassword = isFilled
            )
        }

    }

    fun setConfirmPassword (password: String) {
        var errorMsg = ""
        var isError = false
        val isFilled = password != ""
        if (password != uiState.value.password){
            errorMsg = "passwords do not match"
            isError = true
        }
        _uiState.update {
            it.copy(
                passwordConfirm = password,
                message = errorMsg,
                errorState = isError,
                filledPassword = isFilled
            )
        }
    }

    fun submit(){

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }
            delay(3000)
            try {
                val serverResponse = CrosswordApi.retrofitService.getHello()
                Log.i(TAG, serverResponse)
            }
            catch (e : ConnectException){
                Log.i(TAG, "unable to find server")
            }
            _uiState.update {
                it.copy(isLoading = false)
            }
        }

    }



}

class RegistrationViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistrationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
