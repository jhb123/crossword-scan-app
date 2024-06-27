package com.jhb.crosswordScan.data

import android.util.Log

private const val TAG = "Puzzle"
data class Puzzle(
    val across: Map<String, Clue>,
    val down: Map<String, Clue>,
) {
    val gridSize: Int
        get() {
            var xMax = 0
            var yMax = 0
            across.forEach{ (_, clue) ->
                clue.cells.forEach { cell ->
                    if (cell.x > xMax) xMax = cell.x
                    if (cell.y > yMax) yMax = cell.y
                }
            }
            return xMax + 1
        }
    val clues: Map<String, Clue>
        get() {
            return across + down
        }
    val cells: Set<Cell>
        get() {
            val cellSet = mutableSetOf<Cell>()
            this.clues.forEach { (s, clue) ->
                clue.cells.forEach {
                    if (!cellSet.add(it)) {
                        Log.d(TAG,"Found duplicate")
                    }
                }
                Log.d(TAG, "Adding clue $s")
            }
            return cellSet.toSet()
        }
}

fun getAcrossCluesAsPairs(puzzle: Puzzle) : List<Pair<String, String>>{
    val clues = puzzle.across
    val cluePairs = getCluesAsPairs(clues)
    val cluePairsSorted = cluePairs.sortedBy {
        (it.first.dropLast(1)).toInt()
    }
    return cluePairsSorted
}

fun getDownCluesAsPairs(puzzle: Puzzle) : List<Pair<String, String>>{
    val clues = puzzle.down
    val cluePairs = getCluesAsPairs(clues)
    val cluePairsSorted = cluePairs.sortedBy {
        (it.first.dropLast(1)).toInt()
    }
    return cluePairsSorted
}


private fun getCluesAsPairs(clues : Map<String, Clue>) : List<Pair<String, String>> {
    val cluePairs = clues.map {(name, clue) ->
        Pair(name ,clue.hint)
    }
    return cluePairs
}

fun defaultPuzzle() : Puzzle {
    val puzzleBuilder = PuzzleBuilder()
    val clue1a = clueMaker(Direction.ACROSS, 0 , 0, 8, "For all the money that e'er I had" )
    val clue3a = clueMaker(Direction.ACROSS,  0, 4, 8, "I spent it in good company" )
    val clue1d = clueMaker(Direction.DOWN, 0 , 0, 8, "And for all the harm that ever I've done" )
    val clue2d = clueMaker(Direction.DOWN, 4 , 0, 8, "I've done to none but me." )

    return puzzleBuilder
        .addAcrossClue("1a", clue1a)
        .addAcrossClue("3a", clue3a)
        .addDownClue("1d", clue1d)
        .addDownClue("2d", clue2d)
        .build()
}


fun modifyPuzzleWithCell(puzzle: Puzzle, cell: Cell): Puzzle {
    val across = puzzle.across.mapValues { (_,clue) ->
        updateClueWithCell(clue,cell)
    }
    val down = puzzle.down.mapValues { (_,clue) ->
        updateClueWithCell(clue,cell)
    }
    return puzzle.copy(across=across, down=down)
}

