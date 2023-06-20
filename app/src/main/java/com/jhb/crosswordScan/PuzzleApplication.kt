package com.jhb.crosswordScan

import android.app.Application
import com.jhb.crosswordScan.data.PuzzleRepository
import com.jhb.crosswordScan.data.PuzzleRoomDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


class PuzzleApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { PuzzleRoomDataBase.getDatabase(this,applicationScope) }
    val repository by lazy { PuzzleRepository(database.puzzleDao()) }

}