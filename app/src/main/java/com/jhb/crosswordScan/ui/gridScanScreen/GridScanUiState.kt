package com.jhb.crosswordScan.ui.gridScanScreen

import android.graphics.Bitmap

data class GridScanUiState(
    val gridPicDebug : Bitmap?, // = Bitmap.createBitmap(),
    val gridPicProcessed : Bitmap?, // = Bitmap.createBitmap(),
    val previewWidth: Int = 400,
    val previewHeight: Int = 400
)
