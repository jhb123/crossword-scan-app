package com.jhb.crosswordScan.ui.puzzlePreviewScreen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jhb.crosswordScan.data.Puzzle
import com.jhb.crosswordScan.data.insertPuzzle
import com.jhb.crosswordScan.ui.common.ScanUiState
import com.jhb.crosswordScan.ui.common.clueTextBox
import com.jhb.crosswordScan.ui.gridScanScreen.GridScanUiState
import kotlinx.coroutines.launch

private const val TAG = "puzzlePreviewComposable"
@Composable
fun puzzlePreviewScreen(
    uiGridState: State<GridScanUiState>,
    uiClueState: State<ScanUiState>,
    puzzle: Puzzle
    //onSave : () -> Job//() -> Unit
    //viewModel: CrosswordScanViewModel
) {
    val context = LocalContext.current
    val composableScope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(1f)
            .padding(5.dp)//Modifier.padding(5.dp)
    ) {
        Card(modifier = Modifier
            .width(200.dp)
            .height(200.dp)
            .padding(5.dp)
        ){
            uiGridState.value.gridPicProcessed.let {
                if (it != null) {
                    Image(bitmap = it.asImageBitmap(),
                        contentDescription = "scanned bitmap",
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxSize())
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(1f)
                .fillMaxHeight(0.9f)
                .height(500.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(5.dp)
            ) {
                items(uiClueState.value.acrossClues) { clue ->
                    clueTextBox(clueData = clue,
                        //backgroundColor = MaterialTheme.colorScheme.secondary,
                        //textColor = MaterialTheme.colorScheme.onSecondary
                )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(1f)
            ) {
                items(uiClueState.value.downClues) { clue ->
                    clueTextBox(clueData = clue,
                        //backgroundColor = MaterialTheme.colorScheme.secondary,
                        //textColor = MaterialTheme.colorScheme.onSecondary
                )
                    //Text("${clue.first}) ${clue.second}", modifier = Modifier.padding(5.dp))
                }
            }
        }
        Button(onClick = {
            composableScope.launch{
                insertPuzzle(puzzle,context)
            }
            //Log.i(TAG,"Save button clicked")
            Toast.makeText(context,"Saved",Toast.LENGTH_SHORT).show()
            //onSave()
        }) {
            Text(text = "Save Puzzle")
        }
    }
}

//fun insert() = coroutineScope.launch {
//    Log.i(TAG,"inserting new puzzle")
//    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
//    val currentDate = sdf.format(Date())
//    val id = UUID.randomUUID().toString()
//
//    //val puzzleData = PuzzleData(currentDate, puzzle.value)
//    val puzzleData = PuzzleData(id = id, lastModified = sdf.toString(),puzzle = sdf.toString())
//    repository.insert(puzzleData)
//    PuzzleToJson(puzzle.value)
//
//}
//
//fun insert() = Dispatchers.IO{
//
//}

