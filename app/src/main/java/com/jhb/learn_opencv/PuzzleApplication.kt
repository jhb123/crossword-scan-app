package com.jhb.learn_opencv

import android.app.Application
import com.jhb.learn_opencv.data.PuzzleRepository
import com.jhb.learn_opencv.data.PuzzleRoomDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PuzzleApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { PuzzleRoomDataBase.getDatabase(this,applicationScope) }
    val repository by lazy { PuzzleRepository(database.puzzleDao()) }
}