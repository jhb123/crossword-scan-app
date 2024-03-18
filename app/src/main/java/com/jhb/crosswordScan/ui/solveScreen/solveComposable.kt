package com.jhb.crosswordScan.ui.solveScreen

//import com.jhb.learn_opencv.Puzzle
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
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.jhb.crosswordScan.data.Cell
import com.jhb.crosswordScan.data.Puzzle
import com.jhb.crosswordScan.ui.common.dynamicClueTextBox

private const val TAG = "solveComposable"

@Composable
fun SolveScreenWrapper(puzzleId: String) {//repository: PuzzleRepository,index: Int){

    val puzzleSolveViewModel: PuzzleSolveViewModel = viewModel(
        factory = PuzzleSolveViewModelFactory(
            (LocalContext.current.applicationContext as PuzzleApplication).repository,
            puzzleId
        )
    )
    //var puzzleSolveViewModel = PuzzleSolveViewModel(repository)
    //val count by viewModel.counter.collectAsStateWithLifecycle()
    //puzzleSolveViewModel.setup(index)
    //val puzzleSolveViewModel = PuzzleSolveViewModelFactory(repository,index)


    SolveComposable(
        uiState = puzzleSolveViewModel.uiState.collectAsState(),
        onClueSelect = { puzzleSolveViewModel.updateActiveClueByName(it) },
        setLetter = { puzzleSolveViewModel.setLetter(it) },
        delLetter = { puzzleSolveViewModel.delLetter() },
        updateCurrentCell = { puzzleSolveViewModel.updateCurrentCell(it) },
        updateCurrentClue = { puzzleSolveViewModel.updateActiveClueByCell(it) },
        cellSetFromPuzzle = { puzzleSolveViewModel.convertPuzzleToCellSet(it) },//should this be immutable?
        labelledClues = { puzzleSolveViewModel.getLabelledCells(it) }, //should these even be functions!?
        syncFun = { puzzleSolveViewModel.cloudSync() },
        toggleCollapsed = {puzzleSolveViewModel.toggleCollapseKeyboard()}
    )


}

@Composable
fun SolveComposable(
    uiState: State<PuzzleUiState>,//StateFlow<PuzzleUiState>,
    onClueSelect: (String) -> Unit,
    setLetter: (String) -> Unit,
    delLetter: () -> Unit,
    updateCurrentCell: (Cell) -> Unit,
    updateCurrentClue: (Cell) -> Unit,
    cellSetFromPuzzle: (Puzzle) -> MutableSet<Cell>,//should this be immutable?
    labelledClues: (Puzzle) -> Map<Cell, String>, //should these even be functions!?
    syncFun: () -> Unit,
    toggleCollapsed: () -> Unit
) {

    //val uiState = uiState.collectAsState()
    // val clues = uiState.value.currentPuzzle.clues
    val puzzle = uiState.value.currentPuzzle
    val activeClue = uiState.value.currentClue
    val gridSize = uiState.value.currentPuzzle.gridSize
    val keyboardCollapsed = uiState.value.keyboardCollapsed
    Log.i(TAG, "Grid size $gridSize")

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Create references for the composables to constrain
        val (clueGrid, keyBoard, cluesText) = createRefs()

        Box(modifier = Modifier
            .constrainAs(clueGrid) {
                top.linkTo(parent.top, margin = 0.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)

                //bottom.linkTo(cluesText.top)
            }) {
            clueGrid(
                uiState = uiState,
                updateCurrentCell = updateCurrentCell,
                updateCurrentClue = updateCurrentClue,
                gridSize = gridSize,
                cellSetFromPuzzle = cellSetFromPuzzle,
                labelledClues = labelledClues
            )
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
            clueTextArea(puzzle, onClueSelect = onClueSelect, activeClueName = uiState.value.currentClueName)
        }
        Box(modifier = Modifier
            //.background(MaterialTheme.colorScheme.primary)
            .constrainAs(keyBoard) {
                //top.linkTo(cluesText.bottom)
                bottom.linkTo(parent.bottom, margin = 0.dp)
                width = Dimension.wrapContent

            }) {
            keyBoard(
                setLetter = setLetter,
                delLetter = delLetter,
                syncFun = syncFun,
                isCollapsed = keyboardCollapsed,
                toggleCollapsed = toggleCollapsed
            )
        }

    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun keyBoard(
    setLetter: (String) -> Unit,
    delLetter: () -> Unit,
    syncFun: () -> Unit,
    modifier: Modifier = Modifier,
    isCollapsed : Boolean,
    toggleCollapsed : () -> Unit
) {

    Log.i(TAG, "Composing button ")
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    //var isCollapsed by remember { mutableStateOf(false) }
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
            Column() {
                Row(modifier = Modifier
                    .background(keyboardColour) ) {
                    Button(onClick = { syncFun() }) {
                        Text(text = "Synchronise")
                    }
                }
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
fun clueGrid(
    uiState: State<PuzzleUiState>,
    updateCurrentCell: (Cell) -> Unit,
    updateCurrentClue: (Cell) -> Unit,
    gridSize: Int,
    cellSetFromPuzzle: (Puzzle) -> MutableSet<Cell>,//should this be immutable?
    labelledClues: (Puzzle) -> Map<Cell, String>
) {
    val grid = cellSetFromPuzzle(uiState.value.currentPuzzle)
    val labelledClues = labelledClues(uiState.value.currentPuzzle)

    Log.i(TAG, "calling puzzle layout")
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = Modifier
            .size(width = (gridSize * 25 + 3).dp, height = (gridSize * 25 + 3).dp)
            .background(MaterialTheme.colorScheme.outline)
            .padding(1.dp)
    ) {
        PuzzleLayout(
            onClueSelect = {
                Log.i(TAG, "$it selected")
                updateCurrentCell(it)
                updateCurrentClue(it)
            },
            uiState = uiState.value,
            grid = grid,
            labelledClues = labelledClues,
        )
    }

}

@Composable
fun PuzzleLayout(
    onClueSelect: (Cell) -> Unit,
    uiState: PuzzleUiState,
    grid: MutableSet<Cell>,
    labelledClues: Map<Cell, String>
) {
    Log.i(TAG, "Drawing grid")
    grid.forEach { cell ->
        Log.d(TAG, "Creating box ${cell.x}, ${cell.y}, ${cell.c} from scratch?")
        //cellLetterMap[coord]
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 25.dp, height = 25.dp)
                .offset(x = (cell.x * 25).dp)
                .offset(y = (cell.y * 25).dp)
                .padding(0.8.dp)
                .background(
                    if (cell == uiState.currentCell) {
                        //MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
                        //MaterialTheme.colorScheme.scrim
                        //Color.Green
                        MaterialTheme.colorScheme.primaryContainer
                    } else if (uiState.currentClue.cells.contains(cell)) {
                        //Color.Yellow
                        MaterialTheme.colorScheme.surfaceVariant
                        //MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
                .pointerInput(cell) {
                    detectTapGestures {
                        onClueSelect(cell)
                    }
                }

        ) {
            if (labelledClues[cell] != null) {
                Text(
                    text = labelledClues[cell]!!,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 8.sp,
                    modifier = Modifier.offset(x = (-7).dp, y = (-7).dp)
                )
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
            } else {
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


@Composable
fun clueTextArea(
    puzzle: Puzzle,
    onClueSelect: (String) -> Unit,
    activeClueName: String,
    //viewModel: PuzzleSolveViewModel
) {

    val listStateA = rememberLazyListState()
    val listStateD = rememberLazyListState()

    val acrossClues = puzzle.across.toList()
    val downClues = puzzle.down.toList()
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

        val acrossColumn = LazyColumn(
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
        val downColumn = LazyColumn(
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

