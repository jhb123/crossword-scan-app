package com.example.learn_opencv

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Puzzle::class), version = 1, exportSchema = false)
public abstract class PuzzleRoomDataBase : RoomDatabase() {

    // Annotates class to be a Room Database with a table (entity) of the Word class

        abstract fun puzzleDao(): PuzzleDao

        companion object {
            // Singleton prevents multiple instances of database opening at the
            // same time.
            @Volatile
            private var INSTANCE: PuzzleRoomDataBase? = null

            fun getDatabase(context: Context): PuzzleRoomDataBase {
                // if the INSTANCE is not null, then return it,
                // if it is, then create the database
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        PuzzleRoomDataBase::class.java,
                        "puzzle_database"
                    ).build()
                    INSTANCE = instance
                    // return instance
                    instance
                }
            }
        }


}