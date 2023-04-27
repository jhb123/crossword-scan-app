package com.example.learn_opencv.ui

import com.example.learn_opencv.Puzzle

data class PuzzleUiState (
    val currentPuzzle : Puzzle = Puzzle(),
    val name : String = ""
)