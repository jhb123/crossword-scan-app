package com.jhb.crosswordScan.ui.authScreen

data class AuthUiState(
    val userName : String? = null,
    val userPassword : String? = null,
    val userEmail : String? = null,
    //val users : List<UserData>? = null,
    val serverErrorText : String? = null,
    val isLoading : Boolean = false,
    val validLogin : Boolean = true
)
