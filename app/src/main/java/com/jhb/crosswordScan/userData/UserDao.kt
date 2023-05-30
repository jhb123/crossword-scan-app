package com.jhb.crosswordScan.userData

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {


        @Query("SELECT * FROM user_table")
        fun getUsers(): Flow<List<UserData>>

        @Query("SELECT * FROM user_table WHERE id=:uid")
        fun getUser(uid: Int): Flow<UserData>

        @Insert(onConflict = OnConflictStrategy.IGNORE)
        suspend fun insert(user: UserData)

        @Query("DELETE FROM user_table")
        suspend fun deleteAll()

        @Update(onConflict = OnConflictStrategy.IGNORE)
        fun updatePuzzle(user: UserData)


}