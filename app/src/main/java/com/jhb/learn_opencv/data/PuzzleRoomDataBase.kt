package com.jhb.learn_opencv.data

import android.content.Context
import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "PuzzleRoomDataBase"

@Database(entities = arrayOf(PuzzleData::class), version = 1, exportSchema = false)
@TypeConverters(Converters::class)
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
                        .addCallback(PuzzleDatabaseCallback(scope))
                        .build()
                    INSTANCE = instance
                    // return instance
                    instance
                }
            }
        }

    private class PuzzleDatabaseCallback(
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
            // Delete all content here.
            Log.i(TAG,"Deleting all")
            puzzleDao.deleteAll()

            // Add sample words.
            //var puzzle = PuzzleData("123",Puzzle())
            //puzzleDao.insert(puzzle)
            //puzzle = PuzzleData("456",Puzzle())
            //puzzleDao.insert(puzzle)

            // TODO: Add your own words!
        }
    }


}

class Converters {

    @TypeConverter
    fun PuzzleFromJson(json: String?): Puzzle {
        val typeToken = object : TypeToken<Puzzle>() {}.type
        val puzzle = Gson().fromJson<Puzzle>(json, typeToken)
        return puzzle
    }

    @TypeConverter
    fun PuzzleToJson(puzzle: Puzzle?): String {
        val gson = Gson()
        return gson.toJson(puzzle)
    }

}