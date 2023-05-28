package com.jhb.crosswordScan.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzle_table")
class PuzzleData(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name="timeCreated")
    val timeCreated: String,
    @ColumnInfo(name="lastModified")
    val lastModified: String,
    @ColumnInfo(name= "puzzle")
    val puzzle: String,
    @ColumnInfo(name= "puzzleIcon")
    val puzzleIcon: String,


    //@ColumnInfo(name = "puzzle") val puzzle: Puzzle,
    //@ColumnInfo(name = "icon") val icon_uuid: String
    )
