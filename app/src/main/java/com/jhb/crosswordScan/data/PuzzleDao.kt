package com.jhb.crosswordScan.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzle_table")
    fun getPuzzles(): Flow<List<PuzzleData>>

    @Query("SELECT * FROM puzzle_table WHERE id=:uid")
    fun getPuzzleByID(uid: String): PuzzleData

    @Query("UPDATE puzzle_table SET lastModified=:time WHERE id = :id")
    fun updatePuzzleEditTime(time: String,  id: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(puzzle: PuzzleData)

    @Query("DELETE FROM puzzle_table")
    suspend fun deleteAll()

    @Query("DELETE FROM puzzle_table WHERE id = :id")
    fun deletePuzzle(id: String)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updatePuzzle(puzzle: PuzzleData)

}