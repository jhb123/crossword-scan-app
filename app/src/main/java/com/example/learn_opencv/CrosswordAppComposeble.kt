package com.example.learn_opencv


import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.learn_opencv.navigation.Screen
import com.example.learn_opencv.ui.clueScanScreen.ClueScanScreen
import com.example.learn_opencv.ui.gridScanScreen.gridScanScreen
import com.example.learn_opencv.ui.puzzlePreviewScreen.puzzlePreviewScreen
import com.example.learn_opencv.ui.puzzleSelectionScreen.puzzleSelectionComposable
import com.example.learn_opencv.ui.solveScreen.PuzzleSolveViewModel
import com.example.learn_opencv.ui.solveScreen.SolveScreen
import com.example.learn_opencv.viewModels.CrosswordScanViewModel
import com.example.learn_opencv.viewModels.PuzzleSelectViewModel

private const val TAG = "CrosswordAppActivity"

@Composable
fun CrosswordApp(gridScanViewModel: CrosswordScanViewModel,
                 puzzleSelectViewModel: PuzzleSelectViewModel,
                 puzzleSolveViewModel : PuzzleSolveViewModel,
                 takeImage : () -> Unit
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
                    puzzlePreviewScreen(
                        uiGridState = gridScanViewModel.uiGridState.collectAsState(),
                        uiClueState = gridScanViewModel.uiState.collectAsState(),
                        onSave = { gridScanViewModel.insert() }
                    )
                }

                composable(route = "puzzleSelect"){
                    //navController.navigate("solve")

                    puzzleSelectionComposable(
                        uiState = puzzleSelectViewModel.uiState.collectAsState(),
                        navigateToPuzzle = {
                            puzzleSolveViewModel.setUpPuzzle(it)
                            navController.navigate("solve/${it}")
                        }
                    )
                }

                composable(route = "solve/{puzzleId}", arguments =
                listOf(navArgument("puzzleId") { type = NavType.IntType }))
                    {
                        val puzzleIdx = it.arguments?.getInt("puzzleIdx")

                        SolveScreen(
                            uiState = puzzleSolveViewModel.uiState.collectAsState(),
                            onClueSelect = {puzzleSolveViewModel.updateactiveClue(it)},
                            setLetter = {puzzleSolveViewModel.setLetter(it)},
                            delLetter = {puzzleSolveViewModel.delLetter()},
                            updateCurrentCell = { puzzleSolveViewModel.updateCurrentCell(it) },
                            updateCurrentClue = {puzzleSolveViewModel.updateactiveClue2(it)},
                            cellSetFromPuzzle = {puzzleSolveViewModel.convertPuzzleToCellSet(it)},//should this be immutable?
                            labelledClues = {puzzleSolveViewModel.getLabelledCells(it)} //should these even be functions!?
                        )

                }
            }

//            Box(Modifier.padding(innerPadding)) {
//                currentScreen.screen()
//            }

        }
 //   }
}
