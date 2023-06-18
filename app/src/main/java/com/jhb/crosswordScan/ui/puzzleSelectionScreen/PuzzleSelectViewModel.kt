package com.jhb.crosswordScan.viewModels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.jhb.crosswordScan.data.*
import com.jhb.crosswordScan.network.CrosswordApi
import com.jhb.crosswordScan.ui.puzzleSelectionScreen.PuzzleSelectionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.net.ConnectException
import java.util.zip.ZipInputStream

private const val TAG = "PuzzleSelectViewModel"

class PuzzleSelectViewModel(val repository: PuzzleRepository): ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _uiState = MutableStateFlow(PuzzleSelectionUiState())
    val uiState : StateFlow<PuzzleSelectionUiState> = _uiState

    init {
        Log.i(TAG,"initialising ui")
        viewModelScope.launch {
            //delay(100)
            repository.allPuzzles.collect { puzzleList ->
                _uiState.update {
                    it.copy(
                        puzzles = puzzleList
                    )
                }
            }
        }
    }

    fun getPuzzle(filesDir : File) {

        viewModelScope.launch {

            //check if theres a conflict with your database

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorText = null
                )
            }
            val guid = _uiState.value.searchGuid!!.trim()
            //delay(1000)
            val gson = Gson()
            val search = mapOf(
                "id" to guid,
            )
            val payload = gson.toJson(search)
            val requestBody = RequestBody.create(MediaType.get("application/json"), payload)

            try{
                val response = Session.sessionDataState.value?.let { session->
                    session.token?.let {
                        CrosswordApi.retrofitService.search(
                            "Bearer $it",
                            requestBody
                        )
                    }
                }
                if(response != null){
                    //decode the zip files contents
                    val zf = ZipInputStream(response.byteStream())
                    val files = unzipPuzzleFiles(zf)

                    val image = files["image"]?.let { processImageFile(it) }
                    val puzzleTxt = files["puzzleJson"]?.let { processPuzzleFile(it) }
                    val puzzleData = files["metaData"]?.let { processMetaDataFile(it,filesDir.toString()) }

                    //val filesDir = LocalContext.current.filesDir
                    val puzzleFile = File(filesDir,"$guid.json")
                    val imageFile = File(filesDir,"$guid.png")


                    // Create a BufferedWriter instance to write to the file
                    val writer = BufferedWriter(FileWriter(puzzleFile))

                    // Write the data to the file
                    writer.write(puzzleTxt)

                    //save the image
                    val imageStream = FileOutputStream(imageFile)
                    image!!.compress(Bitmap.CompressFormat.PNG,100,imageStream)

                    // Flush and close the writer to ensure the data is written successfully
                    writer.flush()
                    writer.close()
                    imageStream.flush()
                    imageStream.close()

                    if (puzzleData != null) {
                        repository.insert(puzzleData)
                    }

                }
            }
            catch(e : HttpException){
                Log.e(TAG,e.message())
                _uiState.update {
                    it.copy(
                        errorText = e.message()
                    )
                }
            }
            catch (e : ConnectException){
                Log.e(TAG, "unable to find server")
                _uiState.update {
                    it.copy(
                        errorText = "Unable to find server"
                    )
                }
            }
            finally {
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    fun uploadNewPuzzle(puzzleData: PuzzleData,repository: PuzzleRepository){

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val puzzleFile = File(puzzleData.puzzle)
                val imageFile = File(puzzleData.puzzleIcon)

                val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image",
                        "${puzzleData.id}.png",
                        RequestBody.create(MediaType.get("application/octet-stream"), imageFile)
                    )
                    .addFormDataPart(
                        "puzzle",
                        "${puzzleData.id}.json",
                        RequestBody.create(MediaType.get("application/octet-stream"), puzzleFile)
                    )
                    .addFormDataPart("id", puzzleData.id)
                    .addFormDataPart("timeCreated", puzzleData.timeCreated)
                    .addFormDataPart("lastModified", puzzleData.lastModified)
                    .build()
                if (Session.sessionDataState.value != null) {
                    val Authorization = "Bearer ${Session.sessionDataState.value?.token}"
                    Log.i(TAG, "uploading with $Authorization")
                    val message = CrosswordApi.retrofitService.upload(Authorization, requestBody)
                    Log.i(TAG, message.string())
                }
                puzzleData.isShared = true
                Log.i(TAG, puzzleData.puzzle)
                Log.i(TAG, puzzleData.puzzleIcon)

                repository.update(puzzleData)
            }
        }
    }

    fun updateSearch(text: String){
        _uiState.update {
            it.copy(
                searchGuid = text
            )
        }
    }

//val allPuzzles = repository.allPuzzles.asLiveData()

    //val allPuzzles = repository.allPuzzles.collectAsState(initial = )

    //private val _uiState = MutableStateFlow(PuzzleUiState())
    //val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow() //backing property

    //val allPuzzles: LiveData<List<PuzzleData>> = repository.allPuzzles.asLiveData()

//    val homeUiState: StateFlow<HomeUiState> =
//        itemsRepository.getAllItemsStream()

}

class PuzzleSelectViewModelFactory(private val repository: PuzzleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PuzzleSelectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PuzzleSelectViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
