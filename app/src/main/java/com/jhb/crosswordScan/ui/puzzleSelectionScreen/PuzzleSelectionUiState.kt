package com.jhb.crosswordScan.ui.puzzleSelectionScreen

import android.graphics.Bitmap
import com.jhb.crosswordScan.data.PuzzleData

data class PuzzleSelectionUiState(
    val puzzles : List<PuzzleData> = listOf<PuzzleData>(),
    val searchGuid : String = "",
    val isLoading : Boolean = false,
    val errorText : String? = null,
    val imageTest : Bitmap? = null,
    val isOffline : Boolean = false
)
