package com.example.learn_opencv

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzle_table")
    fun getPuzzles(): Flow<List<PuzzleData>>

    @Query("SELECT * FROM puzzle_table WHERE id=:uid")
    fun getPuzzleByID(uid: String):Flow<PuzzleData>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(puzzle: PuzzleData)

    @Query("DELETE FROM puzzle_table")
    suspend fun deleteAll()
}