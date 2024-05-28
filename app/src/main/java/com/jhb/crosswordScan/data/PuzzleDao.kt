package com.jhb.crosswordScan.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzle_table")
    fun getPuzzles(): Flow<List<PuzzleData>>

    @Query("SELECT MAX(id) FROM puzzle_table LIMIT 1")
    fun getLastIndex(): Int

    @Query("SELECT * FROM puzzle_table WHERE id=:uid")
    fun getPuzzleByID(uid: String): PuzzleData

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(puzzle: PuzzleData)

    @Query("DELETE FROM puzzle_table")
    suspend fun deleteAll()

    @Query("DELETE FROM puzzle_table WHERE id = :id")
    fun deletePuzzle(id: Int)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updatePuzzle(puzzle: PuzzleData)

}