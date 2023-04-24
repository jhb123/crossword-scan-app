package com.example.learn_opencv

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzle_table")
data class PuzzleData(@PrimaryKey @ColumnInfo(name = "puzzle") val puzzle: Puzzle)
