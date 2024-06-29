package com.jhb.crosswordScan


//import androidx.compose.material.BottomNavigation
//import androidx.compose.material3.BottomNavigation
//import androidx.compose.material.BottomNavigationItem
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.jhb.crosswordScan.data.Session
import com.jhb.crosswordScan.navigation.Screen
import com.jhb.crosswordScan.ui.authScreen.AuthScreen
import com.jhb.crosswordScan.ui.common.CrosswordAppUiState
import com.jhb.crosswordScan.ui.puzzleScanningScreens.clueScanScreen.clueScanScreen
import com.jhb.crosswordScan.ui.puzzleScanningScreens.gridScanScreen.gridScanScreen
import com.jhb.crosswordScan.ui.puzzleScanningScreens.puzzlePreviewScreen.puzzlePreviewScreen
import com.jhb.crosswordScan.ui.puzzleSelectionScreen.puzzleSelectionScreen
import com.jhb.crosswordScan.ui.registerScreen.RegistrationScreen
import com.jhb.crosswordScan.ui.resetPasswordScreen.resetPasswordScreen
import com.jhb.crosswordScan.ui.solveScreen.SolveScreenWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


private const val TAG = "CrosswordAppActivity"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrosswordApp(repository: PuzzleRepository) {

    val navController = rememberNavController()
    val uiState = MutableStateFlow(CrosswordAppUiState(pageTitle = ""))
    val sessionDataState by Session.sessionDataState.collectAsState()

    //val darkMode = uiState.collectAsState().value.darkMode
    var darkMode by remember { mutableStateOf(false) }
    AppTheme(useDarkTheme = darkMode) {
        //    var currentScreen: RallyDestination by remember { mutableStateOf(Overview) }
        Scaffold(
            topBar = {
                val pageTitle = uiState.collectAsState().value.pageTitle

                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.navigate(Screen.SelectPuzzle.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            } },
                            content = {
                                Icon(
                                    painterResource(id = R.drawable.ic_baseline_home_24),
                                    contentDescription = stringResource(id = R.string.action_home)
                                )
                            }
                        )
                    },
                    title = {
                        Text(
                            text = pageTitle,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                    actions = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = {
                                    navController.navigate(Screen.Authenticate.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                },
                                content = {
                                    Icon(
                                        painterResource(id = Screen.Authenticate.iconResourceId),
                                        contentDescription = stringResource(id =Screen.Authenticate.resourceId)
                                    )
                                }
                            )
                            sessionDataState?.username?.let {
                                Text(it,
                                    modifier = Modifier
                                        .padding(0.dp)
                                        .offset(y = (-15).dp)
                                )
                            }

                        }


                        IconButton(
                            onClick = { darkMode = !darkMode },
                            content = {
                                Icon(
                                    painterResource(id = R.drawable.ic_baseline_dark_mode_24),
                                    contentDescription = stringResource(id = R.string.contentDesc_darkModeToggele)
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
                            icon = {
                                Icon(
                                    painterResource(screen.iconResourceId),
                                    contentDescription = null
                                )
                            },
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
                startDestination = Screen.Authenticate.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                // this is the composable for scanning grids. At somepoint, the viewmodel should
                // be taken out of the composable and replaced with callbacks.
                composable(route = Screen.GridScan.route) {
                    uiState.update { ui ->
                        ui.copy(pageTitle = stringResource(id = Screen.GridScan.resourceId))
                    }
                    gridScanScreen()
                }

                //
                composable(route = Screen.ClueScan.route) {
                    uiState.update { ui ->
                        ui.copy(pageTitle = stringResource(id = Screen.ClueScan.resourceId))
                    }
                    clueScanScreen()
                }

                composable(route = Screen.PreviewScan.route) {
                    uiState.update { ui ->
                        ui.copy(pageTitle = stringResource(id = Screen.PreviewScan.resourceId))
                    }
                    puzzlePreviewScreen()
                    Log.i(TAG, "Navigated to preview screen")
                }

                composable(route = Screen.SelectPuzzle.route) {
                    uiState.update { ui ->
                        ui.copy(pageTitle = stringResource(id = Screen.SelectPuzzle.resourceId))
                    }
                    Log.i(TAG, "navigated to puzzleSelect")
                    puzzleSelectionScreen(navigateToPuzzle = { puzzleId,remote ->
                            Log.i(TAG, "Navigating to solve/$remote/$puzzleId")
                            navController.popBackStack()
                            navController.navigate("${Screen.Solve.route}/$remote/$puzzleId")
                        })
                }

                composable(
                    route = "${Screen.Solve.route}/{remote}/{puzzleId}",
                    arguments = listOf(navArgument("puzzleId") { type = NavType.StringType }),
                )
                {
                    uiState.update { ui ->
                        ui.copy(pageTitle = stringResource(Screen.Solve.resourceId))
                    }
                    val puzzleId = it.arguments?.getString("puzzleId")
                    val remote = it.arguments?.getString("remote").toBoolean()

                    Log.i(TAG, "navigated to solve/$remote/$puzzleId")
                    if (puzzleId != null) {
                        SolveScreenWrapper(puzzleId, remote
                        ) { navController.navigate(Screen.SelectPuzzle.route) }
                    } else {
                        Log.e(TAG, "puzzle id in route is null")
                    }
                }

                composable(route = Screen.Authenticate.route) {
                    uiState.update { ui ->
                        ui.copy(pageTitle = stringResource(id = Screen.Authenticate.resourceId))
                    }
                    Log.i(TAG, "navigated to authentication")
                    AuthScreen(
                        navigateToRegistration = {navController.navigate(Screen.Registration.route)},
                        navigateToReset = {navController.navigate(Screen.ResetPassword.route)}
                    )
                }

                composable(route = Screen.Registration.route) {
                    uiState.update { ui ->
                        ui.copy(pageTitle = stringResource(id = Screen.Registration.resourceId))
                    }

                    Log.i(TAG, "navigated to registration")
                    RegistrationScreen(
                        navigateOnSuccess = {
                        Log.i(TAG, "Navigating to ${Screen.SelectPuzzle.route}")
                        navController.popBackStack()
                        navController.navigate(Screen.SelectPuzzle.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })

                }

                composable(
                    route = Screen.ResetPassword.route
                ){
                    uiState.update { ui ->
                        ui.copy(pageTitle = stringResource(id = Screen.ResetPassword.resourceId))
                    }
                    resetPasswordScreen(
                        navigateOnSuccess = {
                            Log.i(TAG, "Navigating to ${Screen.SelectPuzzle.route}")
                            navController.popBackStack()
                            navController.navigate(Screen.SelectPuzzle.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}
