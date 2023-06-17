package com.jhb.crosswordScan.ui.resetPasswordScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.jhb.crosswordScan.data.Session
import com.jhb.crosswordScan.data.SessionData
import com.jhb.crosswordScan.network.CrosswordApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.HttpException
import java.net.ConnectException

private const val TAG = "ResetPasswordViewModel"

class ResetPasswordViewModel(navigateOnSuccess : ()->Unit) : ViewModel() {

    private val navigateOnSuccess : ()-> Unit = navigateOnSuccess
    private val _resetState = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState : StateFlow<ResetPasswordUiState> = _uiState
    var userName : String? = null

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

        var serverMessage = ""
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true
                )
            }
            try {
                val serverResponse = CrosswordApi.retrofitService.getResetPassword(uiState.value.email)
                Log.i(TAG, "$serverResponse")
                userName = serverResponse
            }
            catch (e : ConnectException){
                Log.i(TAG, "unable to find server")
            }
            catch(e : HttpException){
                Log.w(TAG, "unable to find user ${uiState.value.email}")
                Log.e(TAG,e.message())
                serverMessage = when{
                    e.code() == 400 -> e.message.toString()
                    else -> "Error ${e.code()} : ${e.message()}}"
                }
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    serverMessage = serverMessage
                )
            }
        }
    }

    fun setNewPassword(){

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            var serverMessage = ""

            try {

                val password = uiState.value.newpassword

                val gson = Gson()
                val registrationData = mapOf(
                    "resetGuid" to uiState.value.resetCode,
                    "username" to userName,
                    "password" to password
                )

                val payload = gson.toJson(registrationData)
                val requestBody = RequestBody.create(MediaType.get("application/json"), payload)

                Log.i(TAG, "request body made")
                val response = CrosswordApi.retrofitService.postResetPassword(requestBody)

                val sessionData = SessionData(
                    username = userName,
                    password = password,
                    token = response.string()
                )

                Session.updateSession(sessionData)


                navigateOnSuccess()

            }
            catch(e : HttpException){
                Log.e(TAG,e.message())
                serverMessage = when{
                    e.code() == 400 -> e.message.toString()
                    else -> "Error ${e.code()} : ${e.message()}}"
                }
            }
            catch (e : ConnectException){
                Log.e(TAG, "unable to find server")
                serverMessage = "Unable to find server"
            }
            finally {
                _uiState.update {
                    it.copy(isLoading = false, serverMessage = serverMessage)
                }
            }
        }
    }
}

class ResetPasswordViewModelFactory(private val navigateOnSuccess: ()->Unit) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResetPasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResetPasswordViewModel(navigateOnSuccess) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
