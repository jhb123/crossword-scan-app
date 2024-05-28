package com.jhb.crosswordScan.viewModels

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.jhb.crosswordScan.data.*
import com.jhb.crosswordScan.network.CrosswordApi
import com.jhb.crosswordScan.ui.Strings
import com.jhb.crosswordScan.ui.puzzleSelectionScreen.PuzzleSelectionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import java.net.ConnectException
import java.net.UnknownHostException

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
                        val message =
                        CrosswordApi.retrofitService.upload(requestBody)
                        Log.i(TAG, message.string())
                }
                catch(e : HttpException){
                    Log.e(TAG,e.message())
                    errorMessage = Strings.unableToFindServer
                }
                catch (e : ConnectException){
                    Log.e(TAG, "unable to find server")
                    errorMessage = Strings.unableToFindServer
                }
                catch (e: UnknownHostException){
                    Log.e(TAG, e.toString())
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
