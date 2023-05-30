package com.jhb.crosswordScan.userData

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

private const val TAG = "UserRepository"


class UserRepository(private val userDao: UserDao)  {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun getUser(id: Int): Flow<UserData> {
        Log.i(TAG, "getting user $id from database")
        //puzzleDao.updatePuzzle(puzzle)
        val userData : Flow<UserData> = userDao.getUser(id)
        return userData
    }

    val allUsers : Flow<List<UserData>> = userDao.getUsers()


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(user: UserData) {
        Log.i(TAG,"Adding user data to database")
        userDao.insert(user)
    }

}