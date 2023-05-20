package com.jhb.learn_opencv.ui.solveScreen

//import com.jhb.learn_opencv.Puzzle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jhb.learn_opencv.R
import com.jhb.learn_opencv.data.Clue
import com.jhb.learn_opencv.data.Puzzle


private const val TAG = "solveComposable"

@Composable
fun SolveScreenWrapper(puzzleSolveViewModel : PuzzleSolveViewModel){//repository: PuzzleRepository,index: Int){

    //var puzzleSolveViewModel = PuzzleSolveViewModel(repository)
    //val count by viewModel.counter.collectAsStateWithLifecycle()
    //puzzleSolveViewModel.setup(index)
    //val puzzleSolveViewModel = PuzzleSolveViewModelFactory(repository,index)


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

@Composable
fun SolveScreen(
    uiState: State<PuzzleUiState>,//StateFlow<PuzzleUiState>,
    onClueSelect: (String) -> Unit,
    setLetter: (String) -> Unit,
    delLetter: () -> Unit,
    updateCurrentCell : (Triple<Int, Int, String>) -> Unit,
    updateCurrentClue :  (Triple<Int, Int, String>) -> Unit,
    cellSetFromPuzzle : (Puzzle) -> MutableSet<Triple<Int, Int, String>>,//should this be immutable?
    labelledClues : (Puzzle) ->  Map<Triple<Int, Int, String>, String> //should these even be functions!?
) {

    //val uiState = uiState.collectAsState()
    val clues = uiState.value.currentPuzzle.clues
    val activeClue = uiState.value.currentClue
    val gridSize = uiState.value.currentPuzzle.gridSize
    Log.i(TAG,"Grid size $gridSize")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
            clueGrid(
                uiState = uiState,
                updateCurrentCell = updateCurrentCell,
                updateCurrentClue = updateCurrentClue,
                gridSize = gridSize,
                cellSetFromPuzzle = cellSetFromPuzzle,
                labelledClues = labelledClues
            )
            clueTextArea(clues, onClueSelect = onClueSelect, activeClue = activeClue)
            keyBoard(setLetter = setLetter, delLetter = delLetter)
        }
    }
}
@Composable
fun keyBoard(setLetter : (String) -> Unit,
             delLetter : () -> Unit
){
    Log.i(TAG,"Composing button ")
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Column() {
        Row (
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            "QWERTYUIOP".forEach {
                Button(onClick = { setLetter(it.toString()) },
                    modifier = Modifier
                        .width(screenWidth / 10)
                        .padding(2.dp)
                ) {
                    Text(text = it.toString(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            "ASDFGHJKL".forEach {
                Button(onClick = { setLetter(it.toString()) },
                    modifier = Modifier
                        .width(screenWidth / 9)
                        .padding(2.dp)
                ) {
                    Text(text = it.toString(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Row (
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            "ZXCVBNM".forEach {
                Button(onClick = { setLetter(it.toString()) },
                    modifier = Modifier
                        .width(screenWidth / 8)
                        .padding(2.dp)
                ) {
                    Text(text = it.toString(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Button(onClick = { delLetter() },
                modifier = Modifier
                    .width(screenWidth / 8)
                    .padding(2.dp)
            ) {
                Image(painter = painterResource(R.drawable.ic_baseline_keyboard_backspace_24),
                    contentDescription = "delete",
                    colorFilter = ColorFilter.tint(Color.White))
            }
        }
    }

}

@Composable
fun clueGrid(
    uiState: State<PuzzleUiState>,
    updateCurrentCell : (Triple<Int, Int, String>) -> Unit,
    updateCurrentClue :  (Triple<Int, Int, String>) -> Unit,
    gridSize : Int,
    cellSetFromPuzzle : (Puzzle) -> MutableSet<Triple<Int, Int, String>>,//should this be immutable?
    labelledClues : (Puzzle) ->  Map<Triple<Int, Int, String>, String>
) {
    val grid = cellSetFromPuzzle(uiState.value.currentPuzzle)
    val labelledClues = labelledClues(uiState.value.currentPuzzle)

    Log.i(TAG, "calling puzzle layout")
    Box(modifier = Modifier
        .size(width = (gridSize * 25).dp, height = (gridSize * 25).dp)
        .background(Color.DarkGray)
    ) {
        PuzzleLayout(
            onClueSelect = {
                Log.i(TAG,"$it selected")
                updateCurrentCell(it)
                updateCurrentClue(it)
            },
            uiState = uiState.value,
            grid = grid,
            labelledClues = labelledClues
        )
    }

}
@Composable
fun PuzzleLayout(
    onClueSelect: (Triple<Int, Int, String>) -> Unit,
    uiState: PuzzleUiState,
    grid :  MutableSet<Triple<Int, Int, String>>,
    labelledClues: Map<Triple<Int, Int, String>,String >
) {
    Log.i(TAG, "Drawing grid")
    grid.forEach { coord ->
        Log.d(TAG, "Creating box $coord from scratch?")
        //cellLetterMap[coord]
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 25.dp, height = 25.dp)
                .offset(x = (coord.first * 25).dp)
                .offset(y = (coord.second * 25).dp)
                .padding(1.dp)
                .background(
                    if (coord == uiState.currentCell) {
                        Color.Green
                    } else if (uiState.currentClue.clueBoxes.contains(coord)) {
                        Color.Yellow
                    } else {
                        Color.White
                    }
                )
                .pointerInput(coord) {
                    detectTapGestures {
                        onClueSelect(coord)
                    }
                }

        ){
            if (labelledClues[coord] != null){
                Text(text = labelledClues[coord]!!,
                    fontSize = 8.sp,
                    modifier = Modifier.offset(x = (-7).dp, y = (-7).dp))
                Text(
                    text = coord.third,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .offset(x = (0).dp)
                        .alpha(0.4f)
                )
            }
            else{
                Text(
                    text = coord.third,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .offset(x = (0).dp)
                        .alpha(0.4f)

                )

            }

        }

    }
}



@Composable
fun clueTextArea(cluesTxt : Map<String, Clue>,
                 onClueSelect: (String) -> Unit,
                 activeClue: Clue,
    //viewModel: PuzzleSolveViewModel
){
    val listStateA = rememberLazyListState()
    val listStateD = rememberLazyListState()
    //val coroutineScope = rememberCoroutineScope()

    val allClues = cluesTxt.toList()
    val acrossClues = allClues.filter { it.first.contains("a") }.sortedBy { it.first.dropLast(1).toInt()}
    val downClues = allClues.filter { it.first.contains("d") }.sortedBy { it.first.dropLast(1).toInt()}
    val activeIndexA = acrossClues.indexOfFirst { (_, clue) -> clue == activeClue }
    val activeIndexD = downClues.indexOfFirst { (_, clue) -> clue == activeClue }

    Log.i(TAG,"across idx: $activeIndexA, down idx: $activeIndexD")

    //val cluesTxt  = viewModel.uiState.collectAsState().value.currentPuzzle.clues
    //val active

    Row(horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(1f)
    ){
        val acrossColumn = LazyColumn(
            state = listStateA,
            modifier = Modifier
                //.fillMaxWidth(0.9f)
                .height(150.dp)
        ){
            items(acrossClues){ clue->
                Log.i(TAG,"clue: ${clue.first} ${clue.second.clue}")
                if(clue.second == activeClue){
                    clueTextBox(clueData = clue,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        onClueSelect = onClueSelect
                    )
                }
                else{
                    clueTextBox(clueData = clue,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary,
                        onClueSelect = onClueSelect
                    )
                }
            }
        }
        val downColumn = LazyColumn(
            state = listStateD,
            modifier = Modifier
                //.fillMaxWidth(0.01f)
                .height(150.dp)
        ){
            items(downClues){ clue->
                if(clue.second == activeClue){
                    clueTextBox(clueData = clue,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        onClueSelect = onClueSelect
                    )
                }
                else{
                    clueTextBox(clueData = clue,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary,
                        onClueSelect = onClueSelect
                    )
                }
            }
        }
    }
//    CoroutineScope(Dispatchers.Main).launch{
//        Log.i(TAG,"scrolling across list to $activeIndexA")
//        if(activeIndexA>=0){
//            listStateA.animateScrollToItem(activeIndexA)
//        }
//    }
    LaunchedEffect(activeIndexA){
        Log.i(TAG,"scrolling across list to $activeIndexA")
        if(activeIndexA>=0){
            listStateA.animateScrollToItem(activeIndexA)
        }
    }
    LaunchedEffect(activeIndexD){
        Log.i(TAG,"scrolling across down list to $activeIndexD")
        if(activeIndexD>=0){
            listStateD.animateScrollToItem(activeIndexD)
        }
    }

}


@Composable
fun clueTextBox(clueData : Pair<String, Clue>,
                backgroundColor: Color,
                textColor: Color,
                onClueSelect: (String) -> Unit ) {
    Box(
        modifier = Modifier
            //.background(color = backgroundColor)
            .padding(5.dp)
            .width(170.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(5.dp))
            .pointerInput(clueData.second.clueName) {
                detectTapGestures {
                    onClueSelect(clueData.second.clueName)
                }
            }
    ){

        Text(
            "${clueData.first}) ${clueData.second.clue}",
            color = textColor,
            //shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                //.fillMaxWidth(0.5f)
                .padding(5.dp)

            //.clip(RoundedCornerShape(20.dp))
            //shape = RoundedCornerShape(20.dp)
        )
    }
}

