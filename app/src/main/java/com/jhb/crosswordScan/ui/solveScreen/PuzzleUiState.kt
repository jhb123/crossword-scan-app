package com.jhb.crosswordScan.ui.solveScreen

//import com.jhb.learn_opencv.Puzzle
import com.jhb.crosswordScan.data.Cell
import com.jhb.crosswordScan.data.Clue

data class PuzzleUiState (
    val name : String = "",
    val currentCell : Cell = Cell(-1,-1,""),
    val currentClue : Clue = Clue(mutableListOf(),"no data"),
    val currentClueName: String = "",
    val updateFromRepository : Boolean = true,
    val puzzleId : Int? = null,
    val keyboardCollapsed : Boolean = false,
    val cells: List<Cell> = listOf(),
    val labeledCells: Map<Cell, String> = mapOf(),
    val acrossClues: List<Pair<String, Clue>> = listOf(),
    val downClues: List<Pair<String, Clue>> = listOf(),
    val gridSize: Int = 0
    )