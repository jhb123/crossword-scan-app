package com.jhb.crosswordScan.ui.authScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhb.crosswordScan.data.Session
import com.jhb.crosswordScan.data.SessionData
import kotlinx.coroutines.flow.StateFlow


@Composable
fun AuthScreen(
    navigateToRegistration: () -> Unit,
    navigateToReset: () -> Unit

){
    val authViewModel : AuthViewModel = viewModel()

    AuthScreenComposable(
        uiState = authViewModel.uiState.collectAsState(),
        userNameFieldCallback = { authViewModel.setUserName(it) },
        passwordFieldCallback = { authViewModel.setPassword(it) },
        loginCallback = { username, password ->
            authViewModel.login(
                username,
                password
            )
        },
        logoutCallback = { Session.logOut() },
        registerCallback = { navigateToRegistration() },
        forgotPasswordCallback = { navigateToReset() },
        sessionDataState = Session.sessionDataState,
        tokenState = Session.tokenState
    )

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthScreenComposable(
    uiState: State<AuthUiState>,
    userNameFieldCallback: (String) -> Unit,
    passwordFieldCallback: (String) -> Unit,
    loginCallback: (String, String) -> Unit,
    logoutCallback: () -> Unit,
    registerCallback: () -> Unit,
    forgotPasswordCallback: () -> Unit,
    sessionDataState : StateFlow<SessionData?>,
    tokenState: StateFlow<String?>,
) {


    //Session.readUser()
    val sessionFromFile = sessionDataState.collectAsState()
    val tokenFromFile = tokenState.collectAsState()

    val userName = uiState.value.userName
    val password = uiState.value.userPassword

    if (sessionFromFile.value == null) {
        loginComposeable(
            uiState = uiState,
            userNameFieldCallback = { userNameFieldCallback(it) },
            passwordFieldCallback = { passwordFieldCallback(it) },
            loginCallback = { username, password -> loginCallback(username, password) },
            registerCallback = { registerCallback() },
            forgotPasswordCallback = {forgotPasswordCallback()}
            //userDataState = userFromFile,
            //tokenState =
        )
    } else {
        logoutComposeable(
            uiState = uiState,
            logoutCallback = logoutCallback
        )
    }

}
