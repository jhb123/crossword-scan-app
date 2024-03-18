package com.jhb.crosswordScan.data

import android.util.Log

private const val TAG = "puzzle"

class Puzzle() {

    @Transient
    var gridSize : Int = 0

    @Transient
    private var _across = mutableMapOf<String, Clue>()
    @Transient
    private var _down = mutableMapOf<String, Clue>()
    val across : Map<String, Clue> = _across
    val down : Map<String, Clue> = _down
    val clues:  Map<String, Clue>
        get() {
            return _across + _down
        }

    fun setCluesAfterDeserialised(){
        _across = across.toMutableMap()
        _down = down.toMutableMap()
        calculateSize()
    }

    fun calculateSize() {
        var x_max = 0
        var y_max = 0
        across.forEach{ (_, clue) ->
            clue.cells.forEach { cell ->
                if (cell.x > x_max) x_max = cell.x
                if (cell.y > y_max) y_max = cell.y
            }
        }
        gridSize = x_max + 1
    }

    fun updateClueTxt(name: String, newtxt : String){
        if(name in _across){
            _across[name]!!.hint = newtxt //hopefully this doesn't break because of the !!
        } else if(name in _down) {
            _down[name]!!.hint = newtxt
        }
        else{
            Log.w(TAG,"Clue $name not in puzzle, so $newtxt skipped ")
        }
    }

    //var image = Bitmap()

    fun addClue(name:String, clue: Clue, direction: Direction){
        when (direction) {
            Direction.ACROSS -> {
                if(name in _across){
                    throw Exception("A clue with this name already exists")
                }
                else{
                    _across[name]= clue
                }
            }
            Direction.DOWN -> {
                if(name in _down){
                    throw Exception("A clue with this name already exists")
                }
                else{
                    _down[name]= clue
                }
            }
        }
    //        _clues = (_across + _down)
    }

}

fun getAcrossCluesAsPairs(puzzle: Puzzle) : List<Pair<String, String>>{
//    val clues = getCluesByPrefix('a', puzzle)
    val clues = puzzle.across
    val cluePairs = getCluesAsPairs(clues)
    val cluePairsSorted = cluePairs.sortedBy {
        (it.first.dropLast(1)).toInt()
    }
    return cluePairsSorted
}

fun getDownCluesAsPairs(puzzle: Puzzle) : List<Pair<String, String>>{
//    val clues = getCluesByPrefix('d', puzzle)
    val clues = puzzle.down
    val cluePairs = getCluesAsPairs(clues)
    val cluePairsSorted = cluePairs.sortedBy {
        (it.first.dropLast(1)).toInt()
    }
    return cluePairsSorted
}

//private fun getCluesByPrefix(prefix: Char, puzzle: Puzzle) : Map<String, Clue> {
//    val clues = puzzle.clues.filterKeys { it.last() == prefix }
//    return clues
//}

private fun getCluesAsPairs(cluePairs : Map<String, Clue>) : List<Pair<String, String>> {
    var cluePairs = cluePairs.map {(name, clue) ->
        Pair(name ,clue.hint)
    }
    return cluePairs
}

fun defaultPuzzle() : Puzzle {
    val puzzle = Puzzle()
    val clue_1a = clueMaker(Direction.ACROSS, 0 , 0, 8, "For all the money that e'er I had" )
    val clue_3a = clueMaker(Direction.ACROSS,  0, 4, 8, "I spent it in good company" )
    val clue_1d = clueMaker(Direction.DOWN, 0 , 0, 8, "And for all the harm that ever I've done" )
    val clue_2d = clueMaker(Direction.DOWN, 4 , 0, 8, "I've done to none but me." )

    puzzle.addClue("1a", clue_1a, Direction.ACROSS)
    puzzle.addClue("3a", clue_3a, Direction.ACROSS)
    puzzle.addClue("1d", clue_1d, Direction.DOWN)
    puzzle.addClue("2d", clue_2d, Direction.DOWN)
    puzzle.gridSize = 8

    return puzzle
}


