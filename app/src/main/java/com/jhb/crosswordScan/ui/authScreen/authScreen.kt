package com.jhb.crosswordScan.ui.authScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhb.crosswordScan.data.Session
import com.jhb.crosswordScan.data.SessionData
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@Composable
fun AuthScreen(
    navigateToRegistration: () -> Unit,
    navigateToReset: () -> Unit

){
    val authViewModel : AuthViewModel = viewModel()
    val composableScope = rememberCoroutineScope()

    val uiState by authViewModel.uiState.collectAsState()


    AuthScreenComposable(
        uiState = uiState,
        userNameFieldCallback = { authViewModel.setUserName(it) },
        passwordFieldCallback = { authViewModel.setPassword(it) },
        loginCallback = { username, password ->
            composableScope.launch {
                authViewModel.login(
                    username,
                    password
                )
            }
        },
        logoutCallback = { Session.logOut() },
        registerCallback = { navigateToRegistration() },
        forgotPasswordCallback = { navigateToReset() },
        sessionDataState = Session.sessionDataState,
    )

}

@Composable
fun AuthScreenComposable(
    uiState: AuthUiState,
    userNameFieldCallback: (String) -> Unit,
    passwordFieldCallback: (String) -> Unit,
    loginCallback: (String, String) -> Unit,
    logoutCallback: () -> Unit,
    registerCallback: () -> Unit,
    forgotPasswordCallback: () -> Unit,
    sessionDataState : StateFlow<SessionData?>,
) {


    //Session.readUser()
    val sessionFromFile = sessionDataState.collectAsState()


    if (sessionFromFile.value == null) {
        LoginComposable(
            uiState = uiState,
            userNameFieldCallback = { userNameFieldCallback(it) },
            passwordFieldCallback = { passwordFieldCallback(it) },
            loginCallback = { username, password -> loginCallback(username, password) },
            registerCallback = { registerCallback() },
            forgotPasswordCallback = {forgotPasswordCallback()},
            //userDataState = userFromFile,
            //tokenState =
        )
    } else {
        LogoutComposable(
            logoutCallback = logoutCallback
        )
    }

}
