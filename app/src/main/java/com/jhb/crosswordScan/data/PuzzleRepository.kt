package com.jhb.crosswordScan.data

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "PuzzleRepository"

class PuzzleRepository(private val puzzleDao: PuzzleDao)  {

    val allPuzzles: Flow<List<PuzzleData>> = puzzleDao.getPuzzles()


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun getPuzzle(id: String): Flow<PuzzleData>{
        Log.i(TAG, "getting puzzle $id from database")
        //puzzleDao.updatePuzzle(puzzle)
        val puzzleData : Flow<PuzzleData> = puzzleDao.getPuzzleByID(id)
        return puzzleData
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(puzzle: PuzzleData) {
        Log.i(TAG,"Adding data to database")
        puzzleDao.insert(puzzle)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
     fun update(puzzle: PuzzleData){
        Log.i(TAG, "updating database")
        puzzleDao.updatePuzzle(puzzle)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun updatePuzzleEditTime(id: String){
        Log.i(TAG, "updating database")
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        puzzleDao.updatePuzzleEditTime(currentDate,id)
    }


}