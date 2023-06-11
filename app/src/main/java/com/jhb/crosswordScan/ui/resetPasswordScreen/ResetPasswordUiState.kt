package com.jhb.crosswordScan.ui.resetPasswordScreen

data class ResetPasswordUiState(
    val resetCode: String = "",
    val email: String = "",
    val newpassword: String = "",
    val isLoading : Boolean = false,
    val serverMessage : String = ""
)
