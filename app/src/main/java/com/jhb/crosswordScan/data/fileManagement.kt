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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.*
import java.util.zip.ZipInputStream

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

        //val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS:ZZZZZ")
        //val currentDateString = sdf.format(Date())
        //val currentDateString = date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val currentTime = TimeStampFormatter().generateTimeStamp()
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
            timeCreated = currentTime,
            lastModified = currentTime,
            puzzle = puzzleFile.toString(),
            puzzleIcon = imageFile.toString()
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

        Log.i(TAG,"Deleting ${puzzleData.puzzleIcon}")
        Log.i(TAG,"Deleting ${puzzleData.puzzle}")
        val iconFile = File(puzzleData.puzzleIcon)
        val puzzleFile = File(puzzleData.puzzle)
        iconFile.delete()
        puzzleFile.delete()
        Log.i(TAG,"Deleted ${puzzleData.puzzleIcon}")
        Log.i(TAG,"Deleted ${puzzleData.puzzle}")

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

fun processMetaDataFile(metaData : ByteArray, filePath: String) : PuzzleData{

    val metaDataString = metaData.toString(Charsets.UTF_8)

    //check meta data is there
    val metaData = metaDataString?.let {
        Json.parseToJsonElement(it) // To a JsonElement
        .jsonObject                     // To a JsonObject
        .toMutableMap()
    }

    //check meta data is valid
    val isMetaDataValid = metaData?.let {
        it.containsKey("id")
                && it.containsKey("puzzle")
                && it.containsKey("timeCreated")
                && it.containsKey("lastModified")
                && it.containsKey("puzzleIcon")
    }

    if(isMetaDataValid!=true){
        throw Exception("meta data not valid")
    }

    Log.i(TAG,metaData["id"].toString())
    Log.i(TAG,metaData["puzzle"].toString())
    Log.i(TAG,metaData["puzzleIcon"].toString())
    Log.i(TAG,metaData["timeCreated"].toString())
    Log.i(TAG,metaData["lastModified"].toString())

    //at this point, we are sure there is valid meta data.
    val puzzleData = PuzzleData(
        id = metaData["id"].toString().let {
            it.substring(1, it.length - 1)
        },
        puzzle = metaData["puzzle"].toString().let {
            "$filePath/${it.substring(1, it.length - 1)}"
        },
        puzzleIcon = metaData["puzzleIcon"].toString().let {
            "$filePath/${it.substring(1, it.length - 1)}"
        },
        timeCreated = metaData["timeCreated"].toString().let {
            it.substring(1, it.length - 1)
        },
        lastModified = metaData["lastModified"].toString().let {
            it.substring(1, it.length - 1)
        },
        isShared = true

    )
    return puzzleData
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
