package com.example.learn_opencv.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.example.learn_opencv.*
import com.example.learn_opencv.R
import com.example.learn_opencv.ui.PuzzleUiState
import com.example.learn_opencv.viewModels.PuzzleSolveViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val TAG = "SolveFragment"

class SolveFragment : Fragment() {

    lateinit private var viewModel: PuzzleSolveViewModel //by activityViewModels{
//        PuzzleSolveViewModelFactory((requireActivity().application as PuzzleApplication).repository)
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val puzzleIdx = arguments?.getInt("puzzle_id")
        viewModel = PuzzleSolveViewModel(
            (requireActivity().application as PuzzleApplication).repository, puzzleIdx!!
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {

                val uiState = viewModel.uiState.collectAsState()
                val clues = uiState.value.currentPuzzle.clues
                val activeClue = uiState.value.currentClue

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
                        clueGrid(viewModel) //this probably should not accept the view model
                        clueTextArea(clues, onClueSelect = {viewModel.updateactiveClue(it)}, activeClue = activeClue)
                        keyBoard(setLetter = {viewModel.setLetter(it)},{viewModel.delLetter()})
                    }
                }
            }
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
fun clueGrid(viewModel: PuzzleSolveViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val grid = viewModel.convertPuzzleToCellSet(uiState.currentPuzzle)
    val labelledClues = viewModel.getLabelledCells(uiState.currentPuzzle)

    val gridSize = viewModel.getPuzzleDim()
    Log.i(TAG, "calling puzzle layout")
        Box(modifier = Modifier
            .size(width = (gridSize * 25).dp, height = (gridSize * 25).dp)
            .background(Color.DarkGray)
        ) {
            PuzzleLayout(
                onClueSelect = {
                    Log.i(TAG,"$it selected")
                    //viewModel.updateSelection(it)
                    viewModel.updateCurrentCell(it)
                    viewModel.updateactiveClue2(it)
                },
                uiState = uiState,
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
fun clueTextArea( cluesTxt : Map<String, Clue>,
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
fun clueTextBox( clueData : Pair<String, Clue>,
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