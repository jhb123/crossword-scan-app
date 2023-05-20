package com.example.learn_opencv


import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.learn_opencv.data.PuzzleRepository
import com.example.learn_opencv.navigation.Screen
import com.example.learn_opencv.ui.clueScanScreen.ClueScanScreen
import com.example.learn_opencv.ui.gridScanScreen.gridScanScreen
import com.example.learn_opencv.ui.puzzlePreviewScreen.puzzlePreviewScreen
import com.example.learn_opencv.ui.puzzleSelectionScreen.puzzleSelectionComposable
import com.example.learn_opencv.ui.solveScreen.PuzzleSolveViewModel
import com.example.learn_opencv.ui.solveScreen.PuzzleSolveViewModelFactory
import com.example.learn_opencv.ui.solveScreen.SolveScreenWrapper
import com.example.learn_opencv.viewModels.CrosswordScanViewModel
import com.example.learn_opencv.viewModels.PuzzleSelectViewModel


private const val TAG = "CrosswordAppActivity"

@Composable
fun CrosswordApp(gridScanViewModel: CrosswordScanViewModel,
                 puzzleSelectViewModel: PuzzleSelectViewModel,
                 repository: PuzzleRepository,
                 takeImage : () -> Unit,
) {

    val navController = rememberNavController()

    //put all viewmodels and uistates here?

    //RallyTheme {
    //    var currentScreen: RallyDestination by remember { mutableStateOf(Overview) }
        Scaffold(
            topBar = {

            },
            bottomBar = {

                val items = listOf(
                    Screen.GridScan,
                    Screen.ClueScan,
                    Screen.PreviewScan,
                    Screen.SelectPuzzle
                )

                BottomNavigation {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        BottomNavigationItem(
                            icon = { Icon(painterResource(screen.iconResourceId), contentDescription = null) },
                            label = { Text(stringResource(screen.resourceId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->

            val puzzleIdx by remember {mutableStateOf<Int>(0)}

            NavHost(
                navController = navController,
                startDestination = "gridScan",
                modifier = Modifier.padding(innerPadding)
            ) {
                // this is the composable for scanning grids. At somepoint, the viewmodel should
                // be taken out of the composable and replaced with callbacks.
                composable(route = "gridScan") {
                    gridScanScreen(
                        uiState = gridScanViewModel.uiGridState.collectAsState(),
                        viewModel = gridScanViewModel
                    )
                }

                //
                composable(route = "clueScan") {
                    ClueScanScreen(
                        uiState = gridScanViewModel.uiState.collectAsState(),
                        setClueScanDirection = {gridScanViewModel.setClueScanDirection(it)},
                        takeImage = takeImage,
                        onDragStart = { gridScanViewModel.resetClueHighlightBox(it) },
                        onDrag = { gridScanViewModel.changeClueHighlightBox(it) },
                        setCanvasSize = {gridScanViewModel.setCanvasSize(it)},
                        scanClues = {gridScanViewModel.scanClues()}
                    )
                }

                composable(route = "previewScan") {
                    Log.i(TAG, "Navigated to preview screen")
                    puzzlePreviewScreen(
                        uiGridState = gridScanViewModel.uiGridState.collectAsState(),
                        uiClueState = gridScanViewModel.uiState.collectAsState(),
                        onSave = {
                            Log.i(TAG,"inserting...")
                            gridScanViewModel.insert()
                        }
                    )
                }

                composable(route = "puzzleSelect"){
                    puzzleSelectionComposable(
                        uiState = puzzleSelectViewModel.uiState.collectAsState(),
                        navigateToPuzzle = {
                            Log.i(TAG,"Navigating to solve/$it")
                            navController.navigate("solve/$it")
                        }
                    )
                }

                composable(
                    route = "solve/{puzzleId}",
                    arguments = listOf(navArgument("puzzleId") { type = NavType.IntType }),
                )
                    {

                        val puzzleIdx = it.arguments?.getInt("puzzleId")
                        //puzzleApplication.

//                        val puzzleSolveViewModel: PuzzleSolveViewModel by   {
//                            PuzzleSolveViewModelFactory(repository,puzzleIdx!!)
//                        }
                        val puzzleSolveViewModel: PuzzleSolveViewModel = viewModel(factory = PuzzleSolveViewModelFactory(repository,puzzleIdx!!))


                        Log.i(TAG,"navigated to solve/$puzzleIdx")
                        SolveScreenWrapper(puzzleSolveViewModel)

                }
            }
        }
    //} when a theme is made, this can be uncommented.
}
