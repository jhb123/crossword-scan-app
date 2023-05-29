package com.jhb.crosswordScan.ui.common

data class CrosswordAppUiState(
    val pageTitle : String,
    val darkMode : Boolean = false,
    val puzzleSearchShown : Boolean = false
)
