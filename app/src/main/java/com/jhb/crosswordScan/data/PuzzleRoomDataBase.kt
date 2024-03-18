package com.jhb.crosswordScan.data

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jhb.crosswordScan.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "PuzzleRoomDataBase"

@Database(entities = arrayOf(PuzzleData::class), version = 5, exportSchema = false)
public abstract class PuzzleRoomDataBase : RoomDatabase() {

    // Annotates class to be a Room Database with a table (entity) of the Word class

        abstract fun puzzleDao(): PuzzleDao

        companion object {
            // Singleton prevents multiple instances of database opening at the
            // same time.
            @Volatile
            private var INSTANCE: PuzzleRoomDataBase? = null

            fun getDatabase(
                context: Context,
                scope: CoroutineScope
            ): PuzzleRoomDataBase {
                // if the INSTANCE is not null, then return it,
                // if it is, then create the database
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        PuzzleRoomDataBase::class.java,
                        "puzzle_database"
                    )
                        .fallbackToDestructiveMigration()
                        .addCallback(PuzzleDatabaseCallback(context = context, scope = scope))
                        .build()
                    INSTANCE = instance
                    // return instance
                    instance
                }
            }
        }

    private class PuzzleDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {

                    populateDatabase(database.puzzleDao())
                }
            }
        }

        suspend fun populateDatabase(puzzleDao: PuzzleDao) {
            Log.i(TAG,"Deleting all")
            puzzleDao.deleteAll()

            val puzzle = defaultPuzzle()
            val img = BitmapFactory.decodeResource(context.resources, R.drawable.g2672)
            insertPuzzle(puzzle, context, image=img)

//            var puzzle_data = PuzzleData(
//                id="default",
//                puzzle="default_puzzle",
//                timeCreated="the year 3000",
//                lastModified="today",
//                puzzleIcon = "icon",
//            )
//            puzzleDao.insert(puzzle_data)
            //puzzle = PuzzleData("456",Puzzle())
            //puzzleDao.insert(puzzle)

            // TODO: Add your own words!
        }
    }


}
