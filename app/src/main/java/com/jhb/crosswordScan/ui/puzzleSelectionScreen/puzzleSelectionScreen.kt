package com.jhb.crosswordScan.ui.puzzleSelectionScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val TAG = "puzzleSelectionScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun puzzleSelectionComposable(
    uiState : State<PuzzleSelectionUiState>,
    navigateToPuzzle : (Int) ->  Unit
){

    val puzzles = uiState.value.puzzles

    LazyColumn(contentPadding = PaddingValues(10.dp),
        modifier = Modifier
            .padding(0.dp)
            .background(MaterialTheme.colorScheme.background)
    ){
        items(puzzles){ puzzleData->
            Card(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                shape = RoundedCornerShape(10.dp),
                onClick = {
                    Log.i(TAG, "Card clicked")
                    navigateToPuzzle(puzzles.indexOf(puzzleData))
                          },
                modifier = Modifier
                    .height(100.dp)
                    .fillParentMaxWidth(1f)
                    .padding(5.dp)
            ) {
                Box(Modifier.fillMaxSize()) {
                    Text(puzzleData.id, Modifier.align(Alignment.Center))
                }
            }

        }
    }

}