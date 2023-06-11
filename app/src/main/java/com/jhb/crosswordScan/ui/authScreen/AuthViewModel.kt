package com.jhb.crosswordScan.ui.authScreen

import android.util.Log
import androidx.lifecycle.ViewModel
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


private const val TAG = "AuthViewModel"

class AuthViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState : StateFlow<AuthUiState> = _uiState

    fun login(username: String, password: String){
        Log.i(TAG,"Logging in")
        viewModelScope.launch {
            //CrosswordAppRequest(status = 200,message="ok")
            val gson = Gson()
            val loginMessage = mapOf(
                "username" to uiState.value.userName!!.trim(),
                "password" to uiState.value.userPassword!!
            )
            val payload = gson.toJson(loginMessage)
            val requestBody = RequestBody.create(MediaType.get("application/json"), payload)

            Log.i(TAG, "request body made")
            var serverMessage : String? = null
            try {
                val response = CrosswordApi.retrofitService.login(requestBody)
                val responseJson = gson.fromJson(response.string(), MutableMap::class.java)

                val sessionData = SessionData(
                    username = username,
                    password = password,
                    token = responseJson["token"].toString()
                )

                Session.updateSession(sessionData)

                Log.i(TAG, "Token ${responseJson["token"]}")
                Log.i(TAG, "Finished logging in")
            }
            catch(e : HttpException){
                Log.e(TAG,e.message())
                serverMessage = "Error ${e.code()} : ${e.message()}"
            }
            finally {
                _uiState.update {
                    it.copy(serverErrorText = serverMessage)
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

}
