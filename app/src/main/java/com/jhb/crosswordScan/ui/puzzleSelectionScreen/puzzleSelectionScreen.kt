package com.jhb.crosswordScan.ui.puzzleSelectionScreen

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import java.io.File


private val TAG = "puzzleSelectionScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun puzzleSelectionComposable(
    uiState : State<PuzzleSelectionUiState>,
    navigateToPuzzle : (String) ->  Unit
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
                //shape = RoundedCornerShape(10.dp),
                onClick = {
                    Log.i(TAG, "Card clicked")
                    navigateToPuzzle(puzzleData.id)
                    //navigateToPuzzle(puzzles.indexOf(puzzleData))
                          },
                modifier = Modifier
                    .height(100.dp)
                    .fillParentMaxWidth(1f)
                    .padding(5.dp)
            ) {
                //Box(Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize(1f)
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .fillMaxHeight(1f)) {
                        val file = File(puzzleData.puzzleIcon)
                        val bmOptions = BitmapFactory.Options()
                        val bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions)
                        if (bitmap != null) {
                            Image(
                                painter = BitmapPainter(bitmap.asImageBitmap()),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize(1f)
                    ) {
                        Text("Created: ${puzzleData.timeCreated}")
                        Text("Modified: ${puzzleData.lastModified}")
                        //Text(puzzleData.id, Modifier.align(CenterHorizontally))
                    }
                }
                    //Image(painter = puzzleData.i, contentDescription = "puzzle icon" )

                //}
            }

        }
    }

}