package com.jhb.crosswordScan.ui.authScreen

import com.jhb.crosswordScan.userData.UserData

data class AuthUiState(
    val userName : String? = null,
    val users : List<UserData>? = null

)
