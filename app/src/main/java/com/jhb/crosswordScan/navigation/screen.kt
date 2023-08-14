package com.jhb.crosswordScan.navigation

//import com.example.learn_opencv.R

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.jhb.crosswordScan.R

sealed class Screen(val route: String,
                    @StringRes val resourceId: Int,
                    @DrawableRes val iconResourceId : Int
) {
    object GridScan : Screen( "gridScan", R.string.screenTitle_gridScan, R.drawable.ic_baseline_grid_on_24)
    object ClueScan : Screen("clueScan", R.string.screenTitle_clueScan, R.drawable.ic_baseline_text_fields_24)
    object PreviewScan : Screen("previewScan", R.string.screenTitle_previewPuzzleScan, R.drawable.ic_baseline_preview_24)
    object SelectPuzzle : Screen("puzzleSelect", R.string.screenTitle_puzzleSelect, R.drawable.ic_baseline_edit_24)
    object Authenticate : Screen("authenticate", R.string.screenTitle_authenticate, R.drawable.ic_baseline_person_24)
    object Registration : Screen("registration", R.string.screenTitle_registrationScreen, R.drawable.ic_baseline_person_24)
    object ResetPassword : Screen("resetPassword", R.string.screenTitle_resetPassword, R.drawable.ic_baseline_person_24)
    object Solve : Screen("solve", R.string.screenTitle_solveTitle, R.drawable.ic_baseline_edit_24)

    //object SolvePuzzle : Screen("solve", R.string.clueScan, R.drawable.ic_baseline_camera_24)

}