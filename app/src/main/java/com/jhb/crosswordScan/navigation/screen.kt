package com.jhb.crosswordScan.navigation

//import com.example.learn_opencv.R

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.jhb.crosswordScan.R

sealed class Screen(val route: String,
                    @StringRes val resourceId: Int,
                    @DrawableRes val iconResourceId : Int
) {
    object GridScan : Screen( "gridScan", R.string.gridScan, R.drawable.ic_baseline_grid_on_24)
    object ClueScan : Screen("clueScan", R.string.clueScan, R.drawable.ic_baseline_camera_24)
    object PreviewScan : Screen("previewScan", R.string.previewGridScan, R.drawable.ic_baseline_preview_24)
    object SelectPuzzle : Screen("puzzleSelect", R.string.solveMenuItem, R.drawable.ic_baseline_edit_24)
    object Authenticate : Screen("authenticate", R.string.authenticateMenuItem, R.drawable.ic_baseline_person_24)

    //object SolvePuzzle : Screen("solve", R.string.clueScan, R.drawable.ic_baseline_camera_24)

}