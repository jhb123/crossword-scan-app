package com.jhb.crosswordScan


//import androidx.compose.material.BottomNavigation
//import androidx.compose.material3.BottomNavigation
//import androidx.compose.material.BottomNavigationItem
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.compose.AppTheme
import com.jhb.crosswordScan.data.PuzzleRepository
import com.jhb.crosswordScan.navigation.Screen
import com.jhb.crosswordScan.ui.clueScanScreen.ClueScanScreen
import com.jhb.crosswordScan.ui.common.CrosswordAppUiState
import com.jhb.crosswordScan.ui.gridScanScreen.gridScanScreen
import com.jhb.crosswordScan.ui.puzzlePreviewScreen.puzzlePreviewScreen
import com.jhb.crosswordScan.ui.puzzleSelectionScreen.puzzleSelectionComposable
import com.jhb.crosswordScan.ui.solveScreen.PuzzleSolveViewModel
import com.jhb.crosswordScan.ui.solveScreen.PuzzleSolveViewModelFactory
import com.jhb.crosswordScan.ui.solveScreen.SolveScreenWrapper
import com.jhb.crosswordScan.viewModels.CrosswordScanViewModel
import com.jhb.crosswordScan.viewModels.PuzzleSelectViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


private const val TAG = "CrosswordAppActivity"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrosswordApp(gridScanViewModel: CrosswordScanViewModel,
                 puzzleSelectViewModel: PuzzleSelectViewModel,
                 repository: PuzzleRepository,
                 takeImage : () -> Unit,
) {

    val navController = rememberNavController()
    val uiState = MutableStateFlow(CrosswordAppUiState(pageTitle = ""))
    //val darkMode = uiState.collectAsState().value.darkMode
    var darkMode by remember{ mutableStateOf(false) }
    AppTheme(useDarkTheme = darkMode){
    //    var currentScreen: RallyDestination by remember { mutableStateOf(Overview) }
        Scaffold(
            topBar = {
                val pageTitle = uiState.collectAsState().value.pageTitle

                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.navigateUp() },
                            content = {
                                Icon(
                                    painterResource(id = R.drawable.ic_baseline_home_24),
                                    contentDescription = "back"
                                )
                            }
                        ) },
                    title = {
                        Text(
                            text = pageTitle,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineSmall)
                            },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                    actions = {
                        IconButton(
                        onClick = {darkMode = !darkMode},
                        content = {
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_dark_mode_24),
                                contentDescription = "dark mode"
                            )
                        }
                        )
                    }
                )
            },
            bottomBar = {

                val items = listOf(
                    Screen.GridScan,
                    Screen.ClueScan,
                    Screen.PreviewScan,
                    Screen.SelectPuzzle
                )

                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        NavigationBarItem(
                            alwaysShowLabel = false,
                            icon = { Icon(painterResource(screen.iconResourceId), contentDescription = null) },
                            label = {
                                Text(
                                    stringResource(screen.resourceId),
                                    //color = MaterialTheme.colorScheme.onPrimary
                                )
                                    },
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
                                    //
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = "gridScan",
                modifier = Modifier.padding(innerPadding)
            ) {
                // this is the composable for scanning grids. At somepoint, the viewmodel should
                // be taken out of the composable and replaced with callbacks.
                composable(route = "gridScan") {
                    uiState.update {ui ->
                        ui.copy(pageTitle = stringResource(id = R.string.gridScan))
                    }
                    gridScanScreen(
                        uiState = gridScanViewModel.uiGridState.collectAsState(),
                        viewModel = gridScanViewModel
                    )
                }

                //
                composable(route = "clueScan") {
                    uiState.update {ui ->
                        ui.copy(pageTitle = stringResource(id = R.string.clueScan))
                    }
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
                    uiState.update {ui ->
                        ui.copy(pageTitle = stringResource(id = R.string.previewGridScan))
                    }
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
                    uiState.update {ui ->
                        ui.copy(pageTitle = stringResource(id = R.string.solveMenuItem))
                    }
                    Log.i(TAG,"navigated to puzzleSelect")

                    puzzleSelectionComposable(
                        uiState = puzzleSelectViewModel.uiState.collectAsState(),
                        navigateToPuzzle = {
                            Log.i(TAG,"Navigating to solve/$it")
                            navController.popBackStack()
                            navController.navigate("solve/$it")
                        }
                    )
                }

                composable(
                    route = "solve/{puzzleId}",
                    arguments = listOf(navArgument("puzzleId") { type = NavType.IntType }),
                )
                    {
                        uiState.update {ui ->
                            ui.copy(pageTitle = "Puzzle")
                        }
                        val puzzleIdx = it.arguments?.getInt("puzzleId")
                        val puzzleSolveViewModel: PuzzleSolveViewModel = viewModel(factory = PuzzleSolveViewModelFactory(repository,puzzleIdx!!))

                        Log.i(TAG,"navigated to solve/$puzzleIdx")
                        SolveScreenWrapper(puzzleSolveViewModel)

                }
            }
        }
    }
}
