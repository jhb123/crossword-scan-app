package com.jhb.crosswordScan.data

import android.util.Log

private const val TAG = "puzzle"

class Puzzle() {

    var gridSize : Int = 0

    //should be a way of stopping external object changing this?
    private val _clues = mutableMapOf<String, Clue>()
    val clues : Map<String, Clue> = _clues

    fun updateClueTxt(name: String, newtxt : String){
        if(name in _clues){
            _clues[name]!!.clue = newtxt //hopefully this doesn't break because of the !!
        }
        else{
            Log.w(TAG,"Clue $name not in puzzle, so $newtxt skipped ")
        }
    }

    //var image = Bitmap()

    fun addClue(name:String, clue: Clue){
        if(name in _clues){
            throw Exception("A clue with this name already exists")
        }
        else{
            _clues[name]= clue
        }

    }

}

fun getAcrossCluesAsPairs(puzzle: Puzzle) : List<Pair<String, String>>{
    val clues = getCluesByPrefix('a', puzzle)
    val cluePairs = getCluesAsPairs(clues)
    val cluePairsSorted = cluePairs.sortedBy {
        (it.first.dropLast(1)).toInt()
    }
    return cluePairsSorted
}

fun getDownCluesAsPairs(puzzle: Puzzle) : List<Pair<String, String>>{
    val clues = getCluesByPrefix('d', puzzle)
    val cluePairs = getCluesAsPairs(clues)
    val cluePairsSorted = cluePairs.sortedBy {
        (it.first.dropLast(1)).toInt()
    }
    return cluePairsSorted
}

private fun getCluesByPrefix(prefix: Char, puzzle: Puzzle) : Map<String, Clue> {
    val clues = puzzle.clues.filterKeys { it.last() == prefix }
    return clues
}

private fun getCluesAsPairs(cluePairs : Map<String, Clue>) : List<Pair<String, String>> {
    var cluePairs = cluePairs.map {
        Pair(it.value.clueName ,it.value.clue)
    }
    return cluePairs
}



