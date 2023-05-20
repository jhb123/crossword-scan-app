package com.jhb.learn_opencv.ui.common

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

data class ScanUiState(
    //clue scanning
    val cluePicDebug : Bitmap?, // = Bitmap.createBitmap(),
    val croppedCluePic : Bitmap?, // = Bitmap.createBitmap(),
    val gridPic : Bitmap?,
    val selectedPoints : MutableList<Offset>, //
    val canvasOffset: Offset,
    val canvasSize : Size,
    val isScrollingCanvas : Boolean,
    val clueScanDirection: ClueDirection,
    val acrossClues : List<Pair<String,String>>,
    val downClues : List<Pair<String,String>>
)

enum class ClueDirection{
    ACROSS, DOWN
}