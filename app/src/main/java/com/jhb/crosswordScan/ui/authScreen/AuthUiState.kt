package com.jhb.crosswordScan.ui.authScreen

import com.jhb.crosswordScan.userData.UserData

data class AuthUiState(
    val userName : String? = null,
    val userPassword : String? = null,
    val userEmail : String? = null,
    val users : List<UserData>? = null,
    val serverErrorText : String? = null
)
