package com.jhb.crosswordScan.ui.puzzleScanningScreens.puzzlePreviewScreen

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhb.crosswordScan.PuzzleApplication
import com.jhb.crosswordScan.data.Puzzle
import com.jhb.crosswordScan.data.insertPuzzle
import com.jhb.crosswordScan.ui.common.ClickableClueTextBox
import com.jhb.crosswordScan.ui.common.ScanUiState
import com.jhb.crosswordScan.ui.puzzleScanningScreens.gridScanScreen.GridScanUiState
import com.jhb.crosswordScan.ui.puzzleScanningScreens.gridScanScreen.OpenCVlogic
import com.jhb.crosswordScan.viewModels.CrosswordScanViewModel
import com.jhb.crosswordScan.viewModels.CrosswordScanViewModelFactory
import kotlinx.coroutines.launch

private const val TAG = "puzzlePreviewComposable"

@Composable
fun puzzlePreviewScreen(
    viewModel: CrosswordScanViewModel = viewModel(
        factory = CrosswordScanViewModelFactory(
            (LocalContext.current.applicationContext as PuzzleApplication).repository
        ),
        viewModelStoreOwner = (LocalContext.current as ComponentActivity)
    )
){

    val GridState by viewModel.uiGridState.collectAsState()
    val ClueState by viewModel.uiState.collectAsState()
    val puzzle = viewModel.puzzle.value

    val openCVlogic = OpenCVlogic(viewModel)
    puzzlePreviewComposable(
        uiGridState = GridState,
        uiClueState = ClueState,
        puzzle = puzzle,
        updateClueText = { oldClue, newClue -> viewModel.replaceClueText(oldClue, newClue)}
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun puzzlePreviewComposable(
    uiGridState: GridScanUiState,
    uiClueState: ScanUiState,
    puzzle: Puzzle,
    updateClueText : ( Pair<String,String>,Pair<String,String> ) -> Unit

    //onSave : () -> Job//() -> Unit
    //viewModel: CrosswordScanViewModel
) {
    val context = LocalContext.current
    val composableScope = rememberCoroutineScope()

    //val acrossClues = puzzle.clues.map {  }

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
            uiGridState.gridPicProcessed.let {
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
                //.height(500.dp)
        ) {
            var openDialog by remember { mutableStateOf(false) }
            var oldClue by remember { mutableStateOf(Pair("","")) }
            var newClue by remember { mutableStateOf(Pair("","")) }


            if (openDialog) {
                AlertDialog(
                    onDismissRequest = { openDialog = false }
                )
                {
                    Surface(
                        modifier = Modifier
                            .wrapContentWidth()
                            .wrapContentHeight(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column() {

                            Text(
                                text = "Edit ${oldClue.first}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(20.dp)
                            )
                            OutlinedTextField(
                                value = newClue.second,
                                onValueChange = {newClue = Pair(newClue.first, it)},
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                minLines = 6,
                                maxLines = 6,
                            )

                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        //composableScope.launch(Dispatchers.IO) {
                                        updateClueText( oldClue , newClue )
                                        openDialog = false
                                        //}
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )

                                ) {
                                    Text(text = "Confirm")
                                }
                                OutlinedButton(onClick = { openDialog = false }) {
                                    Text(text = "Cancel")
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(5.dp)
            ) {
                items(uiClueState.acrossClues) { clue ->
                    ClickableClueTextBox(
                        clueData = clue,
                        onClick =  {
                            openDialog = true
                            oldClue = clue
                            newClue = clue

                        }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(1f)
            ) {
                items(uiClueState.downClues) { clue ->
                    ClickableClueTextBox(
                        clueData = clue,
                        onClick =  {
                            openDialog = true
                            oldClue = clue
                            newClue = clue

                        }
                    )
                    //Text("${clue.first}) ${clue.second}", modifier = Modifier.padding(5.dp))
                }
            }
        }
        Button(onClick = {
            composableScope.launch{
                insertPuzzle(puzzle,context,uiGridState.gridPicProcessed)
            }
            //Log.i(TAG,"Save button clicked")
            Toast.makeText(context,"Saved",Toast.LENGTH_SHORT).show()
            //onSave()
        }) {
            Text(text = "Save Puzzle")
        }
    }
}

