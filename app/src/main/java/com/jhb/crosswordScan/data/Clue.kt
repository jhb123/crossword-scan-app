package com.jhb.crosswordScan.data

import java.io.Serializable


enum class Direction {
    ACROSS, DOWN
}
data class Clue(
    val cells : List<Cell>,
    val hint: String = "" ){
}

fun updateClueWithCell(clue: Clue, cell: Cell): Clue {
    val cells = clue.cells.map{
        if(it.x == cell.x && it.y == cell.y) {
            cell
        }
        else {
            it
        }
    }
    return clue.copy(cells=cells)
}

fun clueMaker(direction : Direction, start_x: Int, start_y: Int, len: Int, hint: String = "Default") : Clue {
    val nums = 0..(len-1)
    val cells = when (direction) {
        Direction.ACROSS -> nums.map { i -> Cell(x = start_x+i, y = start_y, c = "") }
        Direction.DOWN -> nums.map { i -> Cell(x = start_x, y = start_y+i, c = "") }
    }

    return Clue(cells=cells, hint=hint)
}

data class Cell(val x: Int, val y: Int, val c: String) : Serializable, Comparable<Cell> {
    override fun compareTo(other: Cell): Int {
        val yDelta = y.compareTo(other.y)
        val xDelta = x.compareTo(other.x)
        if (yDelta > 0) {
            return yDelta
        } else if( yDelta < 0) {
            return yDelta
        } else {
            return xDelta
        }
    }

    override fun toString(): String = "{\"x\":$x,\"y\":$y,\"c\":\"$c\"}"

}