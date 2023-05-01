package com.example.learn_opencv.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.learn_opencv.*
import com.example.learn_opencv.R
import com.example.learn_opencv.viewModels.PuzzleSolveViewModel
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,

                        ) {
                        clueGrid(viewModel)
                        keyBoard(viewModel)
                    }
                }
            }
        }
    }
}


@Composable
fun keyBoard(viewModel: PuzzleSolveViewModel){
    Log.i(TAG,"Composing button ")
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Column() {
        Row (
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            "QWERTYUIOP".forEach {
                Button(onClick = { viewModel.setLetter(it.toString()) },
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
                Button(onClick = { viewModel.setLetter(it.toString()) },
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
                Button(onClick = { viewModel.setLetter(it.toString()) },
                    modifier = Modifier
                        .width(screenWidth / 8)
                        .padding(2.dp)
                ) {
                    Text(text = it.toString(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Button(onClick = { viewModel.delLetter() },
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

    val gridSize = viewModel.getPuzzleDim()
    Log.i(TAG, "calling puzzle layout")
        Box(modifier = Modifier
            .size(width = (gridSize * 25).dp, height = (gridSize * 25).dp)
            .background(Color.DarkGray)
        ) {
            val puzzleLayout = PuzzleLayout(
                onClueSelect = {
                    viewModel.updateactiveClue2(it)
                    viewModel.updateCurrentCell(it)
                },
                currentClue = viewModel.activeClue2, grid = uiState.currentPuzzle,
                currentCell = viewModel.currentCell,
                cellLetterMap = viewModel.cellLetterMap
            )

    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PuzzleLayout(
    onClueSelect: (Pair<Int, Int>) -> Unit,
    currentClue: Clue,
    currentCell: Pair<Int, Int>,
    grid: Puzzle,
    cellLetterMap : MutableMap<Pair<Int,Int> , String>,
) {

    val cellSet = mutableSetOf<Pair<Int, Int>>()
    grid.clues.forEach { s, clue ->
        clue.clueBoxes.forEach {
            cellSet.add(it)
        }
    }


    Log.i(TAG, "Drawing grid")
    cellSet.forEach { coord ->
        Log.i(TAG, "Creating box $coord from scratch?")
        //cellLetterMap[coord]
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 25.dp, height = 25.dp)
                .offset(x = (coord.first * 25).dp)
                .offset(y = (coord.second * 25).dp)
                .padding(1.dp)
                .background(
                    if (coord == currentCell) {
                        Color.Green
                    } else if (currentClue.clueBoxes.contains(coord)) {
                        Color.Yellow
                    } else {
                        Color.White
                    }
                )
                .pointerInput(Unit) {
                    detectTapGestures {
                        Log.i(TAG, "Tapped $coord")
                        onClueSelect(coord)
                    }
                }

        ){
             cellLetterMap[coord]?.let { Text(text = it,
                 textAlign = TextAlign.Center,
             )}
        }
    
    }
}
