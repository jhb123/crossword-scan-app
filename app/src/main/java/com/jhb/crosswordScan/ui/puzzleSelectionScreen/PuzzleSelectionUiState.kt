package com.jhb.crosswordScan.ui.puzzleSelectionScreen

import com.jhb.crosswordScan.data.PuzzleData

data class PuzzleSelectionUiState(
    val puzzles : List<PuzzleData> = listOf<PuzzleData>()
)
