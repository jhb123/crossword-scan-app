package com.example.learn_opencv.ui

import com.example.learn_opencv.Clue
import com.example.learn_opencv.Puzzle

data class PuzzleUiState (
    val currentPuzzle : Puzzle = Puzzle(),
    val name : String = "",
    var currentCellCoord : Pair<Int,Int> = Pair(-1,-1),
    val clues : Map<String, Clue> = mapOf("noname" to Clue("noname", listOf<Pair<Int,Int>>() )),
    var currentClue : Clue = Clue("noname", listOf<Pair<Int,Int>>() ),
    var cellLetterMap : MutableMap<Pair<Int,Int>,String> = mutableMapOf(Pair(-1,-1) to "test")
)

data class CellUiState (
    var text : Char
        )