package com.example.learn_opencv.ui

import com.example.learn_opencv.Clue
import com.example.learn_opencv.Puzzle

data class PuzzleUiState (
    val currentPuzzle : Puzzle = Puzzle(),
    val name : String = "",
    var currentCell : Triple<Int,Int,String> = Triple(-1,-1,""),
    val clues : Map<String, Clue> = mapOf("noname" to Clue("noname", mutableListOf<Triple<Int,Int,String>>() )),
    var currentClue : Clue = Clue("noname", mutableListOf<Triple<Int,Int,String>>() ),
    //var grid : MutableSet<Triple<Int,Int,String>> = mutableSetOf()
    //var cellLetterMap : MutableMap<Pair<Int,Int>,String> = mutableMapOf(Pair(-1,-1) to "test")
)

data class CellUiState (
    var text : Char
        )