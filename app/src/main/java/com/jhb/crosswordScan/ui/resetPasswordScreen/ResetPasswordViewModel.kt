package com.jhb.crosswordScan.ui.resetPasswordScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhb.crosswordScan.network.CrosswordApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException

private const val TAG = "ResetPasswordViewModel"

class ResetPasswordViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState : StateFlow<ResetPasswordUiState> = _uiState

    fun setEmail(input: String){
        _uiState.update {
            it.copy(email = input)
        }
    }

    fun setNewPassword(input: String){
        _uiState.update {
            it.copy(newpassword = input)
        }
    }


    fun setResetCode(input: String){
        _uiState.update {
            it.copy(resetCode = input)
        }
    }

    fun requestPasswordReset() {

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true
                )
            }
            try {
                val serverResponse = CrosswordApi.retrofitService.getResetPassword(uiState.value.email)
                Log.i(TAG, "$serverResponse")
            }
            catch (e : ConnectException){
                Log.i(TAG, "unable to find server")
            }
            catch(e : HttpException){
                Log.w(TAG, "unable to find user ${uiState.value.email}")
            }
            _uiState.update {
                it.copy(
                    isLoading = false
                )
            }
        }
    }




}