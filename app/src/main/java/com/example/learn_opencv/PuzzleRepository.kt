package com.example.learn_opencv

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

private val TAG = "PuzzleRepository"

class PuzzleRepository(private val puzzleDao: PuzzleDao)  {

    val allPuzzles: Flow<List<PuzzleData>> = puzzleDao.getPuzzles()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(puzzle: PuzzleData) {
        Log.i(TAG,"Adding data to database")
        puzzleDao.insert(puzzle)
    }

}