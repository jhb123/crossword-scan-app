package com.example.learn_opencv.viewModels

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.*
import com.example.learn_opencv.data.PuzzleRepository
import com.example.learn_opencv.ui.puzzleSelectionScreen.PuzzleSelectionUiState
import com.example.learn_opencv.ui.solveScreen.PuzzleSolveViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
