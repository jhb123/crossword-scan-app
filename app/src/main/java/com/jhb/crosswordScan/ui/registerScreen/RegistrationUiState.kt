package com.jhb.crosswordScan.ui.registerScreen

data class RegistrationUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val message: String = "",
    val errorState: Boolean = true,
    val passwordsMatch: Boolean = true,
    val isLoading: Boolean = false
)
