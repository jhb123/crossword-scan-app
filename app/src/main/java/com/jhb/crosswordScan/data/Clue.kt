package com.jhb.crosswordScan.data

import java.io.Serializable


enum class Direction {
    ACROSS, DOWN
}
data class Clue(
    val cells : MutableList<Cell>,
//    val direction: Direction = Direction.ACROSS,
    var hint: String = "" ){
//    val cells = cells
//    val direction = direction
//    var hint = hint
}

fun clueMaker(direction : Direction, start_x: Int, start_y: Int, len: Int, hint: String = "Default") : Clue {
    val nums = 0..(len-1)
    val cells = when (direction) {
        Direction.ACROSS -> nums.map { i -> Cell(x = start_x+i, y = start_y, c = "") }
        Direction.DOWN -> nums.map { i -> Cell(x = start_x, y = start_y+i, c = "") }
    }

    return Clue(cells=cells.toMutableList(), hint=hint)
}

data class Cell(val x: Int, val y: Int, var c: String) : Serializable {
    override fun toString(): String = "{\"x\":$x,\"y\":$y,\"c\":\"$c\"}"

}