package com.jhb.crosswordScan.data

import android.content.Context
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jhb.crosswordScan.PuzzleApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

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


suspend fun insertPuzzle(puzzle: Puzzle, context: Context):Boolean = withContext(Dispatchers.IO) {

    //(context.application as PuzzleApplication).repository
    return@withContext try {
        val repository = (context.applicationContext as PuzzleApplication).repository

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        val id = UUID.randomUUID().toString()
        val puzzleData = PuzzleData(id = id, lastModified = currentDate,puzzle = currentDate)
        val puzzleTxt = PuzzleToJson(puzzle)


        val filesDir: File = context.applicationContext.filesDir
        val file = File(filesDir,"$id.json")

        // Create a BufferedWriter instance to write to the file
        val writer = BufferedWriter(FileWriter(file))

        // Write the data to the file
        writer.write(puzzleTxt)

        // Flush and close the writer to ensure the data is written successfully
        writer.flush()
        writer.close()
        repository.insert(puzzleData)

        true
    } catch (e: Exception) {
        false
    }

}

