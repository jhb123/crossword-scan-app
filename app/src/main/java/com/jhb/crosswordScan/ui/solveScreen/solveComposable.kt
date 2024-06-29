package com.jhb.crosswordScan.ui.solveScreen

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhb.crosswordScan.PuzzleApplication
import com.jhb.crosswordScan.R
import com.jhb.crosswordScan.data.Clue
import com.jhb.crosswordScan.network.isInternetAvailable
import com.jhb.crosswordScan.ui.common.dynamicClueTextBox
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "solveComposable"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolveScreenWrapper(puzzleId: String, remote: Boolean, navigation: ()->Unit) {

    val viewModel: PuzzleSolveViewModel = viewModel(
        factory = PuzzleSolveViewModelFactory(
            (LocalContext.current.applicationContext as PuzzleApplication).repository,
            puzzleId, remote
        )
    )

    val uiState = viewModel.uiState.collectAsState()

    val keyboardCollapsed = uiState.value.keyboardCollapsed
    Log.i(TAG, "Composing solve composable")

    OfflineAlertWrapper(puzzleId, remote, navigation)

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Create references for the composables to constrain
        val (clueGrid, keyBoard, cluesText) = createRefs()
        // Text(text = "$serverUpdated")
        Box(modifier = Modifier
            .constrainAs(clueGrid) {
                top.linkTo(parent.top, margin = 0.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)

                //bottom.linkTo(cluesText.top)
            }) {

            Log.i(TAG, "composing grid")
            Box(
                contentAlignment = Alignment.TopStart,
                modifier = Modifier
                    .size(
                        width = (uiState.value.gridSize * 25 + 3).dp,
                        height = (uiState.value.gridSize * 25 + 3).dp
                    )
                    .background(MaterialTheme.colorScheme.outline)
                    .padding(1.dp)
            ) {

                uiState.value.cells.forEach { cell ->
                    // for some reason, I couldn't extract this into a composable with
                    // it recomposing all of the grid.
                    key(cell) {
                        val color = if (cell == uiState.value.currentCell) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else if (uiState.value.currentClue.cells.contains(cell)) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                        val label = uiState.value.labeledCells[cell]

                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 25.dp, height = 25.dp)
                                .offset(x = (cell.x * 25).dp)
                                .offset(y = (cell.y * 25).dp)
                                .padding(0.8.dp)
                                .background(
                                    color = color
                                )
                                .pointerInput(cell) {
                                    detectTapGestures {
                                        viewModel.updateCurrentCell(cell)
                                        viewModel.updateActiveClueByCell(cell)
                                    }
                                }

                        ) {
                            label?.also {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 8.sp,
                                    modifier = Modifier.offset(x = (-7).dp, y = (-7).dp)
                                )
                            }

                            Text(
                                text = cell.c,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .offset(x = (0).dp)
                                    .alpha(0.9f)
                            )
                        }
                    }
                }
            }
        }


        Box(modifier = Modifier
            //.heightIn(50.dp,200.dp)
            .constrainAs(cluesText) {
                top.linkTo(clueGrid.bottom, margin = 0.dp)
                if(keyboardCollapsed) {
                    bottom.linkTo(parent.bottom, margin = 0.dp)
                }
                else{
                    bottom.linkTo(keyBoard.top, margin = 0.dp)
                }
                height = Dimension.fillToConstraints
            }
        ) {
            ClueTextArea(
                acrossClues = uiState.value.acrossClues,
                downClues = uiState.value.downClues,
                onClueSelect = { viewModel.updateActiveClueByName(it) },
                activeClueName = uiState.value.currentClueName
            )
        }
        Box(modifier = Modifier
            //.background(MaterialTheme.colorScheme.primary)
            .constrainAs(keyBoard) {
                //top.linkTo(cluesText.bottom)
                bottom.linkTo(parent.bottom, margin = 0.dp)
                width = Dimension.wrapContent

            }) {
            KeyBoard(
                setLetter = { viewModel.setLetter(it) },
                delLetter = { viewModel.delLetter() },
                isCollapsed = keyboardCollapsed,
                toggleCollapsed = {viewModel.toggleCollapseKeyboard()}
            )
        }

    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun KeyBoard(
    setLetter: (String) -> Unit,
    delLetter: () -> Unit,
    isCollapsed : Boolean,
    toggleCollapsed : () -> Unit
) {

    Log.i(TAG, "Composing button ")
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp
    val keyboardColour = MaterialTheme.colorScheme.background

    AnimatedContent(
        targetState = !isCollapsed,
        transitionSpec = {
            fadeIn(animationSpec = tween(150, 150)) with
                    fadeOut(animationSpec = tween(150)) using
                    SizeTransform { initialSize, targetSize ->
                        if (targetState) {
                            keyframes {
                                // Expand horizontally first.
                                IntSize(targetSize.width, initialSize.height) at 150
                                durationMillis = 300
                            }
                        } else {
                            keyframes {
                                // Shrink vertically first.
                                IntSize(initialSize.width, targetSize.height) at 150
                                durationMillis = 300
                            }
                        }
                    }
        }
    ) { targetExpanded ->
        if (targetExpanded) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .background(keyboardColour)
                ) {
                    "QWERTYUIOP".forEach {
                        FilledTonalButton(
                            onClick = { setLetter(it.toString()) },
                            shape = RoundedCornerShape(2.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .width(screenWidth / 10)
                                .padding(horizontal = 2.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = it.toString(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .background(keyboardColour)
                ) {
                    "ASDFGHJKL".forEach {
                        FilledTonalButton(
                            onClick = { setLetter(it.toString()) },
                            shape = RoundedCornerShape(2.dp),
                            contentPadding = PaddingValues(0.dp),
                            //elevation = ,
                            modifier = Modifier
                                .width(screenWidth / 9)
                                .padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = it.toString(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .background(keyboardColour)
                ) {
                    FilledTonalButton(
                        onClick = { toggleCollapsed() },
                        shape = RoundedCornerShape(2.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .width(screenWidth / 9)
                            .padding(horizontal = 2.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_arrow_drop_down_24),
                            contentDescription = stringResource(id = R.string.drop_down_keyboard),
                        )
                    }

                    "ZXCVBNM".forEach {
                        FilledTonalButton(
                            onClick = { setLetter(it.toString()) },
                            shape = RoundedCornerShape(2.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .width(screenWidth / 9)
                                .padding(horizontal = 2.dp)

                        ) {
                            Text(
                                text = it.toString(),
                                textAlign = TextAlign.Center,
                                //modifier = Modifier.offset(-5.dp,-5.dp)
                                //fontSize = 1.sp
                                //style = MaterialTheme.typography.displaySmall
                            )
                        }
                    }
                    FilledTonalButton(
                        onClick = { delLetter() },
                        shape = RoundedCornerShape(2.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .width(screenWidth / 9)
                            .padding(horizontal = 2.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_keyboard_backspace_24),
                            contentDescription = stringResource(id = R.string.action_backspace),
                            //colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }
            }

        } else {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.wrapContentWidth()
            ) {
                FilledTonalButton(
                    onClick = { toggleCollapsed() },
                    shape = RoundedCornerShape(2.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .width(screenWidth / 9)
                        .padding(horizontal = 2.dp)
                        .alpha(0.7f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_arrow_drop_down_24),
                        contentDescription = stringResource(id = R.string.drop_down_keyboard),
                        modifier = Modifier.rotate(180f)
                    )
                }
            }
        }
    }
}


@Composable
fun ClueTextArea(
    acrossClues: List<Pair<String,Clue>>,
    downClues: List<Pair<String,Clue>>,
    onClueSelect: (String) -> Unit,
    activeClueName: String,
    //viewModel: LocalPuzzleSolveViewModel
) {

    val listStateA = rememberLazyListState()
    val listStateD = rememberLazyListState()

    val activeIndexA = acrossClues.indexOfFirst { (name, _) -> name == activeClueName }
    val activeIndexD = downClues.indexOfFirst { (name, _) -> name == activeClueName }

    Log.i(TAG, "across idx: $activeIndexA, down idx: $activeIndexD")

    //val cluesTxt  = viewModel.uiState.collectAsState().value.currentPuzzle.clues
    //val active

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth(1f)
    ) {
        val configuration = LocalConfiguration.current
        val width = configuration.screenWidthDp
        val trim = 15.dp
        val columnWidth = (width / 2).dp - trim

        LazyColumn(
            state = listStateA,
            modifier = Modifier
        ) {
            items(acrossClues) { (name, clue) ->
                val highlighted = name == activeClueName
                dynamicClueTextBox(
                    clueData = Pair(name, clue),
                    backgroundColor = if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    textColor = if (highlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    onClueSelect = onClueSelect,
                    modifier = Modifier
                        .width(columnWidth)
                        .padding(horizontal = 0.dp, vertical = 5.dp)
                )
            }
        }
        LazyColumn(
            state = listStateD,
            modifier = Modifier
        ) {
            items(downClues) { (name, clue) ->
                val highlighted = name == activeClueName
                dynamicClueTextBox(
                    clueData = Pair(name, clue),
                    backgroundColor = if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    textColor = if (highlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    onClueSelect = onClueSelect,
                    modifier = Modifier
                        .width(columnWidth)
                        .padding(horizontal = 0.dp, vertical = 5.dp)
                )
            }
        }
    }

    LaunchedEffect(activeIndexA) {
        Log.i(TAG, "scrolling across list to $activeIndexA")
        if (activeIndexA >= 0) {
            listStateA.animateScrollToItem(activeIndexA)
        }
    }
    LaunchedEffect(activeIndexD) {
        Log.i(TAG, "scrolling across down list to $activeIndexD")
        if (activeIndexD >= 0) {
            listStateD.animateScrollToItem(activeIndexD)
        }
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OfflineAlertWrapper(puzzleId: String, remote: Boolean, navigation: () -> Unit){

    if (!remote) {
        return
    }
    val viewModel: RemotePuzzleSolveViewModel = viewModel(
        factory = PuzzleSolveViewModelFactory(
            (LocalContext.current.applicationContext as PuzzleApplication).repository,
            puzzleId, remote
        )
    )

    val uiState = viewModel.uiError.collectAsState()

    val composableScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit){
        composableScope.launch {
            while (true) {
                delay(500)
                viewModel.setOnlineState(isInternetAvailable(context))
            }
        }
    }

    uiState.value.error?.let{
        AlertDialog(
            onDismissRequest = { }
        ) {
            Surface(
                modifier = Modifier
                    .width(200.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = it.message)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = navigation, colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text(text = stringResource(id = R.string.navigate_to_puzzles))
                        }
                    }
                }
            }
        }
    }
}

