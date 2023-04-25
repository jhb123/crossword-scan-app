package com.example.learn_opencv

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PuzzleApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { PuzzleRoomDataBase.getDatabase(this,applicationScope) }
    val repository by lazy { PuzzleRepository(database.puzzleDao()) }
}