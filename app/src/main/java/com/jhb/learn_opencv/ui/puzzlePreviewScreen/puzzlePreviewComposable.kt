package com.jhb.learn_opencv.ui.puzzlePreviewScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.jhb.learn_opencv.ui.common.ScanUiState
import com.jhb.learn_opencv.ui.gridScanScreen.GridScanUiState
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
        modifier = Modifier.background(MaterialTheme.colors.background)//Modifier.padding(5.dp)
    ) {
        uiGridState.value.gridPicProcessed.let {
            if (it != null) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "scanned bitmap",
                    modifier = Modifier
                        .padding(5.dp)
                        .width(150.dp)
                        .height(150.dp)
                )
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
                    .width(150.dp)
            ) {
                items(uiClueState.value.acrossClues) { clue ->
                    Text("${clue.first}) ${clue.second}", modifier = Modifier.padding(5.dp))
                }
            }
            LazyColumn(
                modifier = Modifier
                    .width(150.dp)
            ) {
                items(uiClueState.value.downClues) { clue ->
                    Text("${clue.first}) ${clue.second}", modifier = Modifier.padding(5.dp))
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