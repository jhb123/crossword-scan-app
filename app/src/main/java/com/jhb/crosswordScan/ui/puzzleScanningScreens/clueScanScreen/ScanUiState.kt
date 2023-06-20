package com.jhb.crosswordScan.ui.common

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

data class ScanUiState(
    //clue scanning
    //val cluePicDebug : Bitmap? = null, // = Bitmap.createBitmap(),
    //val croppedCluePic : Bitmap? = null, // = Bitmap.createBitmap(),
    val gridPic : Bitmap? = null,
    val selectedPoints : MutableList<Offset> = mutableListOf<Offset>(), //
    val canvasOffset: Offset =  Offset(0f,0f),
    val canvasSize : Size = Size(0f,0f),
    val isScrollingCanvas : Boolean = false,
    val clueScanDirection: ClueDirection = ClueDirection.ACROSS,
    val acrossClues : List<Pair<String,String>> = listOf<Pair<String,String>>(),
    val downClues : List<Pair<String,String>> = listOf<Pair<String,String>>()
)

enum class ClueDirection{
    ACROSS, DOWN
}
