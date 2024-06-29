package com.jhb.crosswordScan.data

import android.util.Log

class PuzzleBuilder {

    companion object {
        const val TAG = "PuzzleBuilder"
    }

    private var across: MutableMap<String, Clue> = mutableMapOf()
    private var down: MutableMap<String, Clue> = mutableMapOf()

    private val clues:  Map<String, Clue>
        get() {
            return across + down
        }

    fun build(): Puzzle {
        return Puzzle(
            across = across,
            down = down,
        )
    }

    fun fromPuzzle(puzzle: Puzzle): PuzzleBuilder {
        across = puzzle.across.toMutableMap()
        down = puzzle.down.toMutableMap()
        return this
    }

    fun addAcrossClue(name:String, clue: Clue): PuzzleBuilder {
        if(name in across){
            throw Exception("A clue with this name already exists")
        }
        else{
            across[name]= clue
        }
        return this
    }

    fun addDownClue(name:String, clue: Clue): PuzzleBuilder {
        if(name in down){
            throw Exception("A clue with this name already exists")
        }
        else{
            down[name]= clue
        }
        return this
    }

    fun setClueText(name: String, newText : String): PuzzleBuilder {
        when (name) {
            in across -> {
                across[name] = across[name]!!.copy(hint = newText)
            }
            in down -> {
                down[name] = down[name]!!.copy(hint = newText)
            }
            else -> {
                Log.w(TAG,"Clue $name not in puzzle, so $newText skipped ")
            }
        }
        return this
    }
}