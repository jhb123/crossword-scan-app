package com.jhb.crosswordScan.ui.registerScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.jhb.crosswordScan.data.Session
import com.jhb.crosswordScan.data.SessionData
import com.jhb.crosswordScan.network.CrosswordApi
import com.jhb.crosswordScan.ui.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException

private const val TAG = "RegistrationViewModel"

class RegistrationViewModel(navigateOnSuccess: ()-> Unit ) : ViewModel() {

    private val navigateOnSuccess = navigateOnSuccess
    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState : StateFlow<RegistrationUiState> = _uiState

    fun setUserName (username: String) {
        _uiState.update {
            it.copy(username = username)
        }
        validFormInput()
    }

    fun setPassword (password: String) {
        _uiState.update {
            it.copy(
                password = password,
            )
        }
        validFormInput()
    }

    fun setConfirmPassword (password: String) {
        _uiState.update {
            it.copy(
                passwordConfirm = password,
            )
        }
        validFormInput()
    }

    fun submit(){

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }
            var serverMessage = ""

            try {

                val username = uiState.value.username.trim()
                val password = uiState.value.password
                val repeatPassword = uiState.value.passwordConfirm

                val gson = Gson()

                Log.i(TAG, "request body made")

                val response = CrosswordApi.retrofitService.register(username,password,repeatPassword)

                val sessionData = SessionData(username = username)

                Session.updateSession(sessionData)

                Log.i(TAG, "Finished registration")
                navigateOnSuccess()
            }
            catch(e : HttpException){
                Log.e(TAG,e.message())
                //val responseInfo = Gson().fromJson(e.response()?.body().toString(), MutableMap::class.java )
                serverMessage = when{
                    e.code() == 409 -> "Username or email already registered"
                    e.code() == 400 -> e.message.toString()
                    else -> "Error ${e.code()} : ${e.message()}}"
                }
            }
            catch (e : ConnectException){
                Log.e(TAG, "unable to find server")
                serverMessage = "Unable to find server"
            }
            catch (e: UnknownHostException){
                Log.e(TAG, e.toString())
                serverMessage = Strings.unableToFindServer
            }
            catch (e: Exception){
                Log.e(TAG, e.toString())
                serverMessage = Strings.genericServerError
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = serverMessage
                )
            }
        }

    }

    private fun validFormInput(){
        val usernameFilled = uiState.value.username != ""
        val passwordFilled = uiState.value.password != ""
        val passwordConfirmFilled = uiState.value.passwordConfirm != ""

        val isFilled = usernameFilled
                && passwordFilled
                && passwordConfirmFilled

        val passwordsMatch = uiState.value.password == uiState.value.passwordConfirm

        var errorMsg = when {
            !passwordsMatch -> "Passwords do not match."
            //!isFilled -> ""
            else -> ""
        }

        val isError = !isFilled || !passwordsMatch
        _uiState.update {
            it.copy(
                message = errorMsg,
                errorState = isError,
                passwordsMatch = passwordsMatch
            )
        }
    }
}

class RegistrationViewModelFactory(private val navigateOnSuccess: ()->Unit) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistrationViewModel(navigateOnSuccess) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
