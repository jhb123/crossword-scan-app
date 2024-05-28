package com.jhb.crosswordScan.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jhb.crosswordScan.PuzzleApplication
import com.jhb.crosswordScan.util.TimeStampFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.zip.ZipInputStream

private const val TAG = "fileManagement"

@TypeConverter
fun PuzzleFromJson(json: String?): Puzzle {
    val typeToken = object : TypeToken<Puzzle>() {}.type
    val puzzle = Gson().fromJson<Puzzle>(json, typeToken)
    puzzle.setCluesAfterDeserialised()
    return puzzle
}

@TypeConverter
fun PuzzleToJson(puzzle: Puzzle?): String {
    val gson = Gson()
    return gson.toJson(puzzle)
}


suspend fun insertPuzzle(puzzle: Puzzle, context: Context):Boolean = withContext(Dispatchers.IO) {
    Log.i(TAG, "Inserting puzzle into database")
    return@withContext try {
        val repository = (context.applicationContext as PuzzleApplication).repository

        val id = repository.getLastIndex() + 1

        val filesDir: File = context.applicationContext.filesDir
        val puzzleFile = File(filesDir,"$id.json")
        val currentTime = TimeStampFormatter().generateTimeStamp()
        val puzzleTxt = PuzzleToJson(puzzle)


        val writer = BufferedWriter(FileWriter(puzzleFile))
        writer.write(puzzleTxt)

        writer.flush()
        writer.close()

        val puzzleData = PuzzleData(
            id = id,
            timeCreated = currentTime,
            file = puzzleFile.toString(),
            name = "Puzzle scanned at $currentTime"
        )

        repository.insert(puzzleData)

        true
    } catch (e: Exception) {
        e.message?.let { Log.w(TAG, it) }
        false
    }
}

suspend fun deletePuzzleFiles(puzzleData: PuzzleData):Boolean = withContext(Dispatchers.IO)  {
    return@withContext try {
        Log.i(TAG,"Deleting ${puzzleData.file}")
        val puzzleFile = File(puzzleData.file)
        puzzleFile.delete()
        Log.i(TAG,"Deleted ${puzzleData.file}")
        true
    } catch (e: Exception) {
        false
    }
}

suspend fun updatePuzzleFile(filePath: String, puzzle: Puzzle):Boolean = withContext(Dispatchers.IO) {
    Log.i(TAG,"Updating puzzle: $filePath")
    return@withContext try {
        val puzzleTxt = PuzzleToJson(puzzle)

        // Create a BufferedWriter instance to write to the file
        val writer = BufferedWriter(FileWriter(filePath))

        // Write the data to the file
        writer.write(puzzleTxt)

        // Flush and close the writer to ensure the data is written successfully
        writer.flush()
        writer.close()
        //repository.insert(puzzleData)

        true
    } catch (e: Exception) {
        false
    }
}


suspend fun readFileAsPuzzle(filePath: String):
    Puzzle? = withContext(Dispatchers.IO)
{
    return@withContext try {
        val file = File(filePath)
        val reader = file.bufferedReader()
        val fileContents = reader.readText()
        reader.close()
        PuzzleFromJson(fileContents)
    } catch (e: Exception){
        Log.e(TAG,"problem loading $filePath, $e")
        null
    }
}

fun unzipPuzzleFiles(zf: ZipInputStream) : Map<String, ByteArray> {
    val files = zf.use { zipInputStream ->
        generateSequence { zipInputStream.nextEntry }
            .map {
                //Log.i(TAG,it.name)
                when {
                    it.name == "meta_data.json" -> Pair<String,ByteArray>("metaData",zipInputStream.readBytes() )
                    it.name.split(".")[1] == "json" -> Pair<String,ByteArray>("puzzleJson",zipInputStream.readBytes() )
                    it.name.split(".")[1] == "png" -> Pair<String,ByteArray>("image",zipInputStream.readBytes() )
                    else -> Pair<String,ByteArray>(it.name,zipInputStream.readBytes() )
                }
            }.toMap()
    }
    return files
}


fun processImageFile(imageData : ByteArray) :  Bitmap{
        val image = BitmapFactory.decodeByteArray(
            imageData,0,
            imageData.size
        )
        return image
}

fun processPuzzleFile(puzzleFile : ByteArray) : String {
    val puzzle = puzzleFile.toString(Charsets.UTF_8)
    //return PuzzleFromJson(puzzle)
    return puzzle
}
