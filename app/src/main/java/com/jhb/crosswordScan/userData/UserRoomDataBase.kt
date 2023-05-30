package com.jhb.crosswordScan.userData

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

private const val TAG = "UserRoomDataBase"

@Database(entities = arrayOf(UserData::class), version = 1, exportSchema = false)
public abstract class UserRoomDataBase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: UserRoomDataBase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): UserRoomDataBase {
            Log.i(TAG,"Getting user room database")
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val passphrase: ByteArray = SQLiteDatabase.getBytes("iLoveCats".toCharArray())
                val factory = SupportFactory(passphrase)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserRoomDataBase::class.java,
                    "user_database"
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .addCallback(UserDatabaseCallback(scope))
                    .build()
                Log.i(TAG,"Creating user database ${instance!=null}")
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    private class UserDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.userDao())
                }
            }
        }

        suspend fun populateDatabase(userDao: UserDao) {
            // Delete all content here.
            Log.i(TAG,"Deleting all")
            userDao.deleteAll()

            Log.i(TAG,"Adding joe to users")

            // Add sample user.
            var user = UserData(
                email = "jhbriggs23@gmail.com",
                userName = "joe",
                password = "password"
            )
            userDao.insert(user)
        }
    }

}