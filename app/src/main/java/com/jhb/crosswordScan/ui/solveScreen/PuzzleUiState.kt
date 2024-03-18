package com.jhb.crosswordScan.ui.solveScreen

//import com.jhb.learn_opencv.Puzzle
import com.jhb.crosswordScan.data.Cell
import com.jhb.crosswordScan.data.Clue
import com.jhb.crosswordScan.data.Puzzle

data class PuzzleUiState (
    val currentPuzzle : Puzzle = Puzzle(),
    val name : String = "",
    val currentCell : Cell = Cell(-1,-1,""),
    val currentClue : Clue = Clue(mutableListOf(),"no data"),
    val currentClueName: String = "",
    val updateFromRepository : Boolean = true,
    val puzzleId : String? = null,
    val keyboardCollapsed : Boolean = false,
)