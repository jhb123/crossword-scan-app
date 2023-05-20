package com.jhb.crosswordScan.ui.solveScreen

import com.jhb.crosswordScan.data.Clue
//import com.jhb.learn_opencv.Puzzle
import com.jhb.crosswordScan.data.Puzzle

data class PuzzleUiState (
    val currentPuzzle : Puzzle = Puzzle(),
    val name : String = "",
    val currentCell : Triple<Int,Int,String> = Triple(-1,-1,""),
    val currentClue : Clue = Clue("noname", mutableListOf<Triple<Int,Int,String>>() ),
    val updateFromRepository : Boolean = true,
)