package com.jhb.crosswordScan.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jhb.crosswordScan.PuzzleApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "fileManagement"

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


suspend fun insertPuzzle(puzzle: Puzzle, context: Context,image: Bitmap?):Boolean = withContext(Dispatchers.IO) {

    return@withContext try {
        val repository = (context.applicationContext as PuzzleApplication).repository

        if(image == null){
            throw  NullPointerException("no image to save")
        }

        val id = UUID.randomUUID().toString()

        val filesDir: File = context.applicationContext.filesDir
        val puzzleFile = File(filesDir,"$id.json")
        val imageFile = File(filesDir,"$id.png")

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())

        val puzzleTxt = PuzzleToJson(puzzle)


        // Create a BufferedWriter instance to write to the file
        val writer = BufferedWriter(FileWriter(puzzleFile))
        //val Imagewriter = BufferedWriter(FileWriter(imageFile))

        // Write the data to the file
        writer.write(puzzleTxt)

        //save the image
        val imageStream = FileOutputStream(imageFile)
        image.compress(Bitmap.CompressFormat.PNG,100,imageStream)



        // Flush and close the writer to ensure the data is written successfully
        writer.flush()
        writer.close()
        imageStream.flush()
        imageStream.close()

        val puzzleData = PuzzleData(
            id = id,
            timeCreated = currentDate,
            lastModified = currentDate,
            puzzle = puzzleFile.toString(),
            puzzleIcon = imageFile.toString()
        )

        repository.insert(puzzleData)

        true
    } catch (e: Exception) {
        false
    }
}

suspend fun updatePuzzleFile(filePath: String, puzzle: Puzzle):Boolean = withContext(Dispatchers.IO) {
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



//suspend fun readFileAsPuzzle(filePath: String): Puzzle = withContext(Dispatchers.IO)  {
//    return@withContext{
//
//    }
//    withContext(Dispatchers.IO) {
//        val file = File(filePath)
//        if (file.exists() && file.isFile) {
//            val reader = file.bufferedReader()
//            val fileContents = reader.readText()
//            reader.close()
//            return@withContext PuzzleFromJson(fileContents)
//        } else {
//            throw IllegalArgumentException("File not found or is not a regular file.")
//        }
//    }
//}
