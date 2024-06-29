package com.jhb.crosswordScan

import com.google.gson.Gson
import com.jhb.crosswordScan.data.PuzzleFromJson
import com.jhb.crosswordScan.data.defaultPuzzle
import org.junit.Assert
import org.junit.Test

class TestPuzzleSerialisation {
    @Test
    fun puzzle_serialise_clues() {
        val gson = Gson()
        val puzzle = defaultPuzzle()
        val ser = gson.toJson(puzzle)
        val puzzle_deserialised = PuzzleFromJson(ser)
        Assert.assertEquals(puzzle_deserialised.clues, puzzle.clues)
    }
    @Test
    fun puzzle_serialise_across() {
        val gson = Gson()
        val puzzle = defaultPuzzle()
        val ser = gson.toJson(puzzle)
        val puzzle_deserialised = PuzzleFromJson(ser);
        Assert.assertEquals(puzzle_deserialised.across, puzzle.across)
    }
    @Test
    fun puzzle_serialise_down() {
        val gson = Gson()
        val puzzle = defaultPuzzle()
        val ser = gson.toJson(puzzle)
        val puzzle_deserialised = PuzzleFromJson(ser);
        Assert.assertEquals(puzzle_deserialised.down, puzzle.down)
    }
    @Test
    fun puzzle_serialise_gridsize() {
        val gson = Gson()
        val puzzle = defaultPuzzle()
        val ser = gson.toJson(puzzle)
        val puzzle_deserialised = PuzzleFromJson(ser);
        Assert.assertEquals(puzzle_deserialised.gridSize, puzzle.gridSize)
    }
}