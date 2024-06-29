package com.jhb.crosswordScan.ui.puzzleSelectionScreen

import com.jhb.crosswordScan.data.PuzzleData

data class PuzzleSelectionUiState(
    val puzzles : List<PuzzleData> = listOf<PuzzleData>(),
    val errorText : String? = null,
    val isOffline : Boolean = false
)
