package com.example.learn_opencv.viewModels

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.learn_opencv.Clue
import com.example.learn_opencv.Puzzle
import com.example.learn_opencv.PuzzleData
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.learn_opencv.PuzzleRepository
import com.example.learn_opencv.ui.PuzzleUiState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.max

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

//    private val _uiState = MutableStateFlow(PuzzleUiState())


    fun getPuzzleDim() : Int {
        var maxVal = 0
        uiState.value.currentPuzzle.clues.forEach { s, clue ->
            clue.clueBoxes.forEach {
                maxVal = max(maxVal,max(it.first,it.second))
            }
        }
        return maxVal+1 //the index+1 gives the maximum size!
    }


    var currentCell by mutableStateOf(Pair<Int,Int>(0,0))
        private set

    fun updateCurrentCell(coord : Pair<Int,Int>){
        currentCell = coord
    }

    var activeClue2 by mutableStateOf(Clue("", listOf<Pair<Int,Int>>()))
        private set

    fun updateactiveClue2(coord : Pair<Int,Int>) {
        Log.i(TAG, "trying to update clue at $coord. Current Active clue ${activeClue2.clueName} ")
        val allClues = uiState.value.currentPuzzle.clues
        for (clue in allClues) {
            if (clue.value.clueBoxes.contains(coord) && clue.value != activeClue2) {
                Log.i(TAG, "setting active clue at $coord to ${clue.key}")
                activeClue2 = clue.value
                break
            }
        }
    }

    //val imageList = mutableStateMapOf<Int, Uri>()

    val cellLetterMap = mutableStateMapOf<Pair<Int,Int>,String>()

    fun setLetter(letter: String){
        cellLetterMap[currentCell] = letter
        Log.i(TAG,"setting $letter for $currentCell")
        val idx = activeClue2.clueBoxes.indexOf(currentCell)
        if(idx+1 < activeClue2.clueBoxes.size )
            currentCell = activeClue2.clueBoxes[idx+1]
    }

    fun delLetter(){
        cellLetterMap[currentCell] = ""
        Log.i(TAG,"deleting for $currentCell")
        val idx = activeClue2.clueBoxes.indexOf(currentCell)
        if(idx-1 >= 0 )
            currentCell = activeClue2.clueBoxes[idx-1]
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