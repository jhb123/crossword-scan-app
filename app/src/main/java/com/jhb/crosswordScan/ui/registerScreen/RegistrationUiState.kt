package com.jhb.crosswordScan.ui.registerScreen

data class RegistrationUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val message: String = "",
    val errorState: Boolean = false,
    val filledPassword: Boolean = false,
    val isLoading: Boolean = false
)
