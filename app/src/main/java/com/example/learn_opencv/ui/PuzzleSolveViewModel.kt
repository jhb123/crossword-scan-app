package com.example.learn_opencv.viewModels

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.*
import com.example.learn_opencv.Clue
import com.example.learn_opencv.Puzzle
import com.example.learn_opencv.PuzzleData
import com.example.learn_opencv.PuzzleRepository
import com.example.learn_opencv.ui.PuzzleUiState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PuzzleSolveViewModel(private val repository: PuzzleRepository,private val puzzleIdx : Int = 0): ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private const val TAG = "PuzzleSolveViewModel"
    }

//    var UiState: MutableStateFlow<PuzzleUiState> =
//        repository.getPuzzle.map { PuzzleUiState(it.puzzle) }

    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> =
        repository.allPuzzles.map{ PuzzleUiState(name=it[puzzleIdx].id ,currentPuzzle = it[puzzleIdx].puzzle) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = PuzzleUiState()
        )

    //val active_cell = uiState.value.current_cell_coord

    private val activeCell = MutableLiveData<Pair<Int,Int>>()
    fun getActiveCell() = activeCell

    fun setActiveCell(coord : Pair<Int,Int>){
        Log.i(TAG,"setting active cell")
        //uiState.value.currentCellCoord = coord
        setActiveClue(coord)
        activeCell.postValue(coord)
    }

    private val activeClue = MutableLiveData<Clue>()
    fun getActiveClue() = activeClue

    fun setActiveClue(coord : Pair<Int,Int>){
        Log.i(TAG,"setting active clue")
        val allClues = uiState.value.currentPuzzle.clues
        for(clue in allClues){
            if(clue.value.clueBoxes.contains(coord) && clue.value != activeClue.value){
                activeClue.postValue(clue.value)
            }
        }
    }


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