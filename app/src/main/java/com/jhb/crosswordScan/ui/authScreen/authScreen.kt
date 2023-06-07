package com.jhb.crosswordScan.ui.authScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import com.jhb.crosswordScan.userData.UserData
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthScreenComposable(
    uiState: State<AuthUiState>,
    userNameFieldCallback: (String) -> Unit,
    passwordFieldCallback: (String) -> Unit,
    loginCallback: (String, String) -> Unit,
    logoutCallback: () -> Unit,
    registerCallback: () -> Unit,
    testServerCallback: () -> Unit,
    forgotPasswordCallback: () -> Unit,
    userDataState: StateFlow<UserData?>,
    tokenState: StateFlow<String?>,
) {


    //SessionData.readUser()
    val userFromFile = userDataState.collectAsState()
    val tokenFromFile = tokenState.collectAsState()

    val userName = uiState.value.userName
    val password = uiState.value.userPassword

    if (userFromFile.value == null) {
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
