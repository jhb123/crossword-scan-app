package com.example.learn_opencv.data

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzle_table")
class PuzzleData(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "puzzle") val puzzle: Puzzle,
    //@ColumnInfo(name = "icon") val icon_uuid: String
    )
