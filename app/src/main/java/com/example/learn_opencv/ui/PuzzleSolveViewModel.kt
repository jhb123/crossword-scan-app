package com.example.learn_opencv.viewModels

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.*
import com.example.learn_opencv.Puzzle
import com.example.learn_opencv.PuzzleData
import com.example.learn_opencv.PuzzleRepository
import com.example.learn_opencv.ui.PuzzleUiState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PuzzleSolveViewModel(private val repository: PuzzleRepository,private val puzzleIdx : Int = 0): ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

//    var UiState: MutableStateFlow<PuzzleUiState> =
//        repository.getPuzzle.map { PuzzleUiState(it.puzzle) }

    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()
    //val puzzleData = repository.getPuzzle.map { it }
    val puzzleData: StateFlow<PuzzleUiState> =
        repository.allPuzzles.map{ PuzzleUiState(name=it[puzzleIdx].id ,currentPuzzle = it[puzzleIdx].puzzle) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = PuzzleUiState()
        )

//    fun setPuzzle(){
//        _uiState.value = PuzzleUiState(currentPuzzle = puzzleData )

//    }
    //val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

//    fun setPuzzle(){
//        //UiState.clear
//        //UiState.value = PuzzleUiState()
//        _uiState.value = PuzzleUiState(currentPuzzle = puzzleData)
//
//    }


}

class PuzzleSolveViewModelFactory(private val repository: PuzzleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PuzzleSolveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PuzzleSolveViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}