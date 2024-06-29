package com.jhb.crosswordScan.ui.authScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.jhb.crosswordScan.data.Session
import com.jhb.crosswordScan.data.SessionData
import com.jhb.crosswordScan.network.CrosswordApi
import com.jhb.crosswordScan.ui.Strings.genericServerError
import com.jhb.crosswordScan.ui.Strings.invalidCredentials
import com.jhb.crosswordScan.ui.Strings.unableToFindServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException


private const val TAG = "AuthViewModel"

class AuthViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState : StateFlow<AuthUiState> = _uiState

    suspend fun login(){
        Log.i(TAG,"Logging in")
        _uiState.update {
            it.copy(
                isLoading = true,
                validLogin = true
            )
        }

        val username = uiState.value.userName?.let{ it.trim()}?: return
        val password = uiState.value.userPassword ?: return

        Log.i(TAG, "request body made")
        var serverMessage : String? = null
        try {
            CrosswordApi.retrofitService.login(username, password)
            val sessionData = SessionData(username = username)
            Session.updateSession(sessionData)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    serverErrorText = null,
                    validLogin = true
                )
            }

            Log.i(TAG, "Finished logging in")
        }
        catch(e : HttpException){
            Log.e(TAG,e.toString())
            val errorMessage = when {
                e.code() == 401 ->  invalidCredentials
                e.code() >= 500 ->  genericServerError
                else  -> genericServerError
            }
            serverMessage = "${e.code()} : $errorMessage"
            _uiState.update {
                it.copy(
                    serverErrorText = serverMessage,
                    isLoading = false,
                    validLogin = false
                )
            }
        }
        catch (e : ConnectException){
            Log.e(TAG, unableToFindServer)
            serverMessage = unableToFindServer
            _uiState.update {
                it.copy(
                    serverErrorText = serverMessage,
                    isLoading = false,
                    validLogin = false
                )
            }
        }
        catch (e: UnknownHostException){
            Log.e(TAG, e.toString())
            serverMessage = unableToFindServer
            _uiState.update {
                it.copy(
                    serverErrorText = serverMessage,
                    isLoading = false,
                    validLogin = false
                )
            }
        }
        catch (e: Exception){
            Log.e(TAG, e.toString())
            serverMessage = genericServerError
            _uiState.update {
                it.copy(
                    serverErrorText = serverMessage,
                    isLoading = false,
                    validLogin = false
                )
            }
        }
    }

    fun setUserName(name: String){
        Log.i(TAG,"username $name")
        _uiState.update {
            it.copy(userName = name)
        }
    }

    fun setLoading(isLoading: Boolean){
        Log.i(TAG,"setting is loading to $isLoading")
        _uiState.update {
            it.copy(isLoading = isLoading)
        }
    }

    fun setPassword(password: String){
        _uiState.update {
            it.copy(userPassword = password)
        }
    }

}
