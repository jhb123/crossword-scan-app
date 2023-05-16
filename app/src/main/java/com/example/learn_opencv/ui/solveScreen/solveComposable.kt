package com.example.learn_opencv.ui.solveScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.learn_opencv.fragments.clueGrid
import com.example.learn_opencv.fragments.clueTextArea
import com.example.learn_opencv.fragments.keyBoard
import com.example.learn_opencv.ui.PuzzleUiState


@Composable
fun SolveScreen(
    uiState: State<PuzzleUiState>,
    onClueSelect: (String) -> Unit,
    setLetter: (String) -> Unit,
    delLetter: () -> Unit
) {

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
            //clueGrid(viewModel) //this probably should not accept the view model
            clueTextArea(clues, onClueSelect = onClueSelect, activeClue = activeClue)
            keyBoard(setLetter = setLetter, delLetter = delLetter)
        }
    }
}

