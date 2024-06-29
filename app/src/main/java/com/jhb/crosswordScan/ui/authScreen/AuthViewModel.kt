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
import okhttp3.FormBody
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException


private const val TAG = "AuthViewModel"

class AuthViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState : StateFlow<AuthUiState> = _uiState

    suspend fun login(username: String, password: String){
        Log.i(TAG,"Logging in")
        _uiState.update {
            it.copy(
                isLoading = true,
                validLogin = true
            )
        }

        val requestBody = FormBody.Builder()
            .add("username", uiState.value.userName!!.trim())
            .add("password", uiState.value.userPassword!!)
            .build()

        Log.i(TAG, "request body made")
        var serverMessage : String? = null
        try {
            val response = CrosswordApi.retrofitService.login(requestBody)
//            val responseJson = gson.fromJson(response.string(), MutableMap::class.java)
//
            val sessionData = SessionData(username = username)
            Session.updateSession(sessionData)


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
        }
        catch (e : ConnectException){
            Log.e(TAG, unableToFindServer)
            serverMessage = unableToFindServer
        }
        catch (e: UnknownHostException){
            Log.e(TAG, e.toString())
            serverMessage = unableToFindServer
        }
        catch (e: Exception){
            Log.e(TAG, e.toString())
            serverMessage = genericServerError
        }
        finally {
            _uiState.update {
                it.copy(
                    serverErrorText = serverMessage,
                    isLoading = false,
                    validLogin = serverMessage.toBoolean()
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
