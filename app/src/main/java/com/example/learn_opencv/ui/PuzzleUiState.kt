package com.example.learn_opencv.ui

import com.example.learn_opencv.Clue
import com.example.learn_opencv.Puzzle

data class PuzzleUiState (
    val currentPuzzle : Puzzle = Puzzle(),
    val name : String = "",
    var currentCellCoord : Pair<Int,Int> = Pair(0,0),
    val clues : Map<String, Clue> = mapOf("noname" to Clue("noname", listOf<Pair<Int,Int>>() )),
    val current_clue : Clue = Clue("noname", listOf<Pair<Int,Int>>() )
)