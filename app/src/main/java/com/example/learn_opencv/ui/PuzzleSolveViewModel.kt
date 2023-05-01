package com.example.learn_opencv.viewModels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.learn_opencv.Clue
import com.example.learn_opencv.Puzzle
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

    //private val _uiState = MutableStateFlow(PuzzleUiState())

//    val uiState: StateFlow<PuzzleUiState> =
//        repository.allPuzzles.map{
//            PuzzleUiState(name=it[puzzleIdx].id ,
//                currentPuzzle = it[puzzleIdx].puzzle)
//        }.stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
//            initialValue = PuzzleUiState()
//        )


    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState : StateFlow<PuzzleUiState> = _uiState

    init {
        Log.i(TAG,"initialising ui")
        viewModelScope.launch{
            repository.allPuzzles.collect {puzzleList ->
                _uiState.update {ui ->
                    ui.copy(currentPuzzle = puzzleList[puzzleIdx].puzzle,
                        name = puzzleList[puzzleIdx].id,
                        //grid = convertPuzzleToCellSet(puzzleList[puzzleIdx].puzzle)
                    )
                }
            }
        }
    }

    fun convertPuzzleToCellSet(puzzle : Puzzle) : MutableSet<Triple<Int, Int, String>>{
        val cellSet = mutableSetOf<Triple<Int, Int, String>>()
        puzzle.clues.forEach { s, clue ->
            clue.clueBoxes.forEach {
                cellSet.add(it)
            }
        }
        return cellSet
    }


    fun getPuzzleDim() : Int {
        var maxVal = 0
        uiState.value.currentPuzzle.clues.forEach { s, clue ->
            clue.clueBoxes.forEach {
                maxVal = max(maxVal,max(it.first,it.second))
            }
        }
        return maxVal+1 //the index+1 gives the maximum size!
    }


//    fun updateSelection(cell : Triple<Int,Int,String>){
//
//        Log.i(TAG, "trying to update clue at $cell. " +
//                "Current Active clue ${_uiState.value.currentClue.clueName} ")
//        val allClues = uiState.value.currentPuzzle.clues
//        var newClue = uiState.value.currentClue
//        for (clue in allClues) {
//            if (clue.value.clueBoxes.contains(cell) && clue.value != _uiState.value.currentClue) {
//                Log.i(TAG, "setting active clue at $cell to ${clue.key}")
//                newClue = clue.value
//                break
//            }
//        }
//
//        _uiState.update { ui ->
//            ui.copy(
//                currentCell = cell,
//                currentClue = newClue,
//                //grid = convertPuzzleToCellSet(uiState.value.currentPuzzle)
//            )
//        }
//    }

    fun updateCurrentCell(cell : Triple<Int,Int,String>){

        _uiState.update { ui ->
            ui.copy(currentCell = cell)
        }
        //_uiState.value.currentCellCoord = coord
    }

//    var activeClue2 by mutableStateOf(Clue("", listOf<Pair<Int,Int>>()))
//        private set

    fun updateactiveClue2(cell : Triple<Int,Int,String>) {
        Log.i(TAG, "trying to update clue at $cell. " +
                "Current Active clue ${_uiState.value.currentClue.clueName} ")
        val allClues = uiState.value.currentPuzzle.clues
        for (clue in allClues) {
            if (clue.value.clueBoxes.contains(cell) && clue.value != _uiState.value.currentClue) {
                Log.i(TAG, "setting active clue at $cell to ${clue.key}")
                _uiState.update { ui ->
                    ui.copy(currentClue = clue.value)
                }
                break
            }
        }
    }

    //val imageList = mutableStateMapOf<Int, Uri>()

    //val cellLetterMap = mutableStateMapOf<Pair<Int,Int>,String>()


    fun setLetter(letter: String){
        //make a new cell to update the grid with
        val newCell = Triple(
            uiState.value.currentCell.first,
            uiState.value.currentCell.second,
            letter
        )

        //go through the current puzzle and add the new cell to all the clues
        uiState.value.currentPuzzle.clues.forEach{ (key,clue)->
            val idx = clue.clueBoxes.indexOf(uiState.value.currentCell)
            if( idx>=0 ){
                clue.clueBoxes[idx] = newCell
            }
            if(clue == uiState.value.currentClue){
                uiState.value.currentClue.clueBoxes[idx] = newCell
            }
        }
        //update the current cell
        val idx = _uiState.value.currentClue.clueBoxes.indexOf(newCell)
        Log.i(TAG,"current position in clue : $idx. Clue's size ${_uiState.value.currentClue.clueBoxes.size}")
        //increment active if its not the last cell in the clue
        if(idx < _uiState.value.currentClue.clueBoxes.size - 1 ) {
            Log.i(TAG,"updating position in clue : ${idx+1}")
            _uiState.update { ui ->
                ui.copy(
                    currentCell = _uiState.value.currentClue.clueBoxes[idx+1],
                )
            }
        }
        else{
            Log.i(TAG,"Last cell")
            _uiState.update { ui ->
                ui.copy(
                    currentCell = newCell,
                )
            }
        }
    }

    fun delLetter(){
        //make a new cell to update the grid with
        val newCell = Triple(
            uiState.value.currentCell.first,
            uiState.value.currentCell.second,
            ""
        )

        //go through the current puzzle and add the new cell to all the clues
        uiState.value.currentPuzzle.clues.forEach{ (key,clue)->
            val idx = clue.clueBoxes.indexOf(uiState.value.currentCell)
            if( idx>=0 ){
                clue.clueBoxes[idx] = newCell
            }
            if(clue == uiState.value.currentClue){
                uiState.value.currentClue.clueBoxes[idx] = newCell
            }
        }
        //update the current cell
        val idx = _uiState.value.currentClue.clueBoxes.indexOf(newCell)
        Log.i(TAG,"current position in clue : $idx. Clue's size ${_uiState.value.currentClue.clueBoxes.size}")
        //increment active if its not the last cell in the clue
        if(idx > 0 ) {
            Log.i(TAG,"updating position in clue : ${idx-1}")
            _uiState.update { ui ->
                ui.copy(
                    currentCell = _uiState.value.currentClue.clueBoxes[idx-1],
                )
            }
        }
        else{
            Log.i(TAG,"first cell")
            _uiState.update { ui ->
                ui.copy(
                    currentCell = newCell,
                )
            }
        }
    }




}

