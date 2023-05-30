package com.jhb.crosswordScan

import android.app.Application
import com.jhb.crosswordScan.data.PuzzleRepository
import com.jhb.crosswordScan.data.PuzzleRoomDataBase
import com.jhb.crosswordScan.userData.UserRepository
import com.jhb.crosswordScan.userData.UserRoomDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


class PuzzleApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    val userDatabase by lazy { UserRoomDataBase.getDatabase(this,applicationScope) }
    val userRepository by lazy { UserRepository(userDatabase.userDao()) }

    val database by lazy { PuzzleRoomDataBase.getDatabase(this,applicationScope) }
    val repository by lazy { PuzzleRepository(database.puzzleDao()) }

}