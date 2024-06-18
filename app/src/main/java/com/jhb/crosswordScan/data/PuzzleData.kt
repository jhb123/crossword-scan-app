package com.jhb.crosswordScan.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "puzzle_table",  indices = [Index(value = ["serverId"], unique = true)]   )
class PuzzleData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name="timeCreated")
    val timeCreated: String?,
    @ColumnInfo(name= "file")
    val file: String?,
    @ColumnInfo(name= "name")
    val name: String?,
    @ColumnInfo(name = "serverId", )
    var serverId: Int? = null,
    )
