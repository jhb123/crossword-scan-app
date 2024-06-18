package com.jhb.crosswordScan.viewModels

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jhb.crosswordScan.data.*
import com.jhb.crosswordScan.network.CrosswordApi
import com.jhb.crosswordScan.ui.Strings
import com.jhb.crosswordScan.ui.puzzleSelectionScreen.PuzzleSelectionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import java.io.IOException

private const val TAG = "PuzzleSelectViewModel"

class PuzzleSelectViewModel(val repository: PuzzleRepository): ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private const val REQUEST_PERIOD = 30_000L
    }

    private val _uiState = MutableStateFlow(PuzzleSelectionUiState())
    val uiState : StateFlow<PuzzleSelectionUiState> = _uiState

    init {
        viewModelScope.launch{
            while (true) {
                withContext(Dispatchers.IO) {
                    fetch_puzzles()
                    delay(REQUEST_PERIOD)
                }
            }
        }

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

    private suspend fun fetch_puzzles() {
        val listType = object : TypeToken<List<ServerPuzzleListItem>>() {}.type
        val gson = Gson()
        try {
            val crosswords = CrosswordApi.retrofitService.getPuzzleList()
            val serverPuzzles: List<ServerPuzzleListItem> = gson.fromJson(crosswords.string(), listType)
            val sharedToDevicePuzzles = repository.getSharedPuzzles()

            sharedToDevicePuzzles.forEach {
                val res = serverPuzzles.find { serverPuzzle -> serverPuzzle.id==it.serverId }
                if (res == null) {
                    repository.deletePuzzle(it)
                }
            }

            serverPuzzles.map { it ->
                val id = repository.getLastIndex() + 1
                val data = PuzzleData(id, null, null, it.name, it.id)
                repository.insert(data)
            }



        } catch (e: IOException) {
            _uiState.update {
                it.copy(
                    errorText = "not connected",
                    isOffline = true
                )
            }
        }
    }

    fun uploadNewPuzzle(puzzleData: PuzzleData,repository: PuzzleRepository){


        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                val username = Session.sessionDataState.value?.username?:run {
                    _uiState.update {
                        it.copy(
                            errorText = Strings.mustBeSignedInToUpload
                        )
                    }
                    return@withContext
                }

                val puzzleFile = File(puzzleData.file)
                val data = puzzleFile.readText()
                val crossword = PuzzleFromJson(data)
                val payload = mapOf("name" to "$username-${puzzleData.id}", "crossword" to crossword)
                val gson = Gson()
                val jsonPayload = gson.toJson(payload)
                Log.i(TAG,jsonPayload)
                val requestBody = RequestBody.create(MediaType.get("application/json"),jsonPayload)

                var errorMessage : String? = null

                try {
                    Log.i(TAG, "uploading puzzle")
                    val response = CrosswordApi.retrofitService.upload(requestBody)
                    val listType = object : TypeToken<ServerPuzzleListItem>() {}.type
                    val serverPuzzle: ServerPuzzleListItem = gson.fromJson(response.string(), listType)
                    puzzleData.serverId = serverPuzzle.id
                    repository.update(puzzleData)
                }
                catch(e : IOException) {
                    errorMessage = Strings.unableToFindServer
                }
                catch (e: Exception){
                    Log.e(TAG, e.toString())
                    errorMessage = Strings.genericServerError
                }
                finally {
                    _uiState.update {
                        it.copy(
                            errorText = errorMessage
                        )
                    }
                }
            }
        }
    }

    fun updateSearch(text: String) {
        _uiState.update {
            it.copy(
                searchGuid = text
            )
        }
    }

    fun getPuzzle(file: File) {
        Log.w(TAG,"TODO: add search for puzzles")
    }
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
