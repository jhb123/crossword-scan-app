package com.jhb.crosswordScan.ui.puzzlePreviewScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.jhb.crosswordScan.ui.common.ScanUiState
import com.jhb.crosswordScan.ui.common.clueTextBox
import com.jhb.crosswordScan.ui.gridScanScreen.GridScanUiState
import kotlinx.coroutines.Job

private const val TAG = "puzzlePreviewComposable"
@Composable
fun puzzlePreviewScreen(
    uiGridState: State<GridScanUiState>,
    uiClueState: State<ScanUiState>,
    onSave : () -> Job//() -> Unit
    //viewModel: CrosswordScanViewModel
) {

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(1f)
            .padding(5.dp)//Modifier.padding(5.dp)
    ) {
        Box(modifier = Modifier
            .width(200.dp)
            .height(200.dp)
            .padding(5.dp)
            .background(MaterialTheme.colorScheme.inverseSurface)
        ){
            uiGridState.value.gridPicProcessed.let {
                if (it != null) {
                    Image(bitmap = it.asImageBitmap(),
                        contentDescription = "scanned bitmap",
                        modifier = Modifier.padding(5.dp).fillMaxSize())
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(1f)
                .height(500.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(5.dp)
            ) {
                items(uiClueState.value.acrossClues) { clue ->
                    clueTextBox(clueData = clue,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary)
                }
            }
            LazyColumn(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(1f)
            ) {
                items(uiClueState.value.downClues) { clue ->
                    clueTextBox(clueData = clue,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary)
                    //Text("${clue.first}) ${clue.second}", modifier = Modifier.padding(5.dp))
                }
            }
        }
        Button(onClick = {
            //Log.i(TAG,"Save button clicked")
            onSave()
        }) {
            Text(text = "Save Puzzle")
        }
    }
}