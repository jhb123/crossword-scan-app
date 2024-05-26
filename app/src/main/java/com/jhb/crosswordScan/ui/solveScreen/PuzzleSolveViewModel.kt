package com.jhb.crosswordScan.ui.solveScreen

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.jhb.crosswordScan.data.*
import com.jhb.crosswordScan.network.CrosswordApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*


class PuzzleSolveViewModel(private val repository: PuzzleRepository,private val puzzleId : String ): ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private const val TAG = "PuzzleSolveViewModel"
    }
    lateinit var puzzleFilePath : String
    lateinit var iconImageFilePath : String
    lateinit var repoPuzzleData : PuzzleData
    // lateinit var puzzle : Puzzle

    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState : StateFlow<PuzzleUiState> = _uiState

    init {
        Log.i(TAG,"initialising ui")

        viewModelScope.launch {
            //delay(50)
            withContext(Dispatchers.IO) {
                val puzzleData = repository.getPuzzle(puzzleId)
                Log.i(TAG, "Collecting puzzle from database")
                Log.i(TAG, "puzzleData puzzle ${puzzleData.puzzle}")
                Log.i(TAG, "puzzleData id ${puzzleData.id}")
                Log.i(TAG, "puzzleData lastModified ${puzzleData.lastModified}")
                puzzleFilePath = puzzleData.puzzle
                iconImageFilePath = puzzleData.puzzleIcon

                val puzzle = readFileAsPuzzle(puzzleFilePath)
                _uiState.update {
                    it.copy(
                        puzzleId = puzzleData.id,
                        currentPuzzle = puzzle!!
                    )
                }
            }
        }
        Log.i(TAG,"Finished initialising ui")
    }

    fun toggleCollapseKeyboard(){
        _uiState.update { ui ->
            ui.copy(
                keyboardCollapsed = !_uiState.value.keyboardCollapsed,
            )
        }
    }


    fun convertPuzzleToCellSet(puzzle : Puzzle) : MutableSet<Cell>{
        val cellSet = mutableSetOf<Cell>()
        puzzle.clues.forEach { s, clue ->
            clue.cells.forEach {
                if (!cellSet.add(it)) {
                    Log.i(TAG,"Found duplicate")
                }

            }
            Log.i(TAG, "Adding clue ${s}")
        }
        return cellSet
    }

    fun cloudSync(){

        viewModelScope.launch {
            Log.i(TAG,"Sycning")
            val gson = Gson()
            val payload = gson.toJson(uiState.value.currentPuzzle)
            Log.i(TAG, payload)
            //uploadPuzzle()
            //update the local puzzle
            //updatePuzzleFile(puzzleFilePath,uiState.value.currentPuzzle)
        }

        // step1, pass the puzzle to the server.
        // Use the servers response as the new grid.
    }

    private fun getGuid(){
        viewModelScope.launch {
            val guid = CrosswordApi.retrofitService.getGuid()

        }
    }

    fun getLabelledCells(puzzle : Puzzle) : Map<Cell,String > {
        val cellSetLabels = mutableMapOf< Cell,String >()
        puzzle.clues.forEach { s, clue ->

            val regex = Regex("\\d+")
            val processed = regex.find(s)
            if (processed != null) {
                cellSetLabels[clue.cells[0]] = processed.value
            }
            else{
                Log.w(TAG,"Badly labelled clue: $s")
            }
        }
        cellSetLabels.forEach({(k, v)-> Log.i(TAG, "special cells ${k.x},${k.y} with $v") })
        return cellSetLabels
    }

    fun updateCurrentCell(cell : Cell){
        _uiState.update { ui ->
            ui.copy(currentCell = cell)
        }
    }

    fun updateActiveClueByName(name : String){
        _uiState.update { ui->
            ui.copy(
                currentClue = _uiState.value.currentPuzzle.clues[name]!!,
                currentCell = _uiState.value.currentPuzzle.clues[name]!!.cells[0],
                currentClueName = name
            )
        }
    }

    fun updateActiveClueByCell(cell : Cell) {
        Log.i(
            TAG, "trying to update clue at $cell. " +
                "from current Active clue ${_uiState.value.currentClue} ")
        val allClues = uiState.value.currentPuzzle.clues
        for (clue in allClues) {
            Log.i(TAG, "current cell: $cell\nClue cells${clue.value.cells}\nContains: ${clue.value.cells.contains(cell)}")
            if (clue.value.cells.contains(cell) && clue.value != _uiState.value.currentClue) {
                Log.i(TAG, "setting active clue at $cell to ${clue.key}")
                _uiState.update { ui ->
                    //if ui.currentClue !=
                    ui.copy(currentClue = clue.value, currentClueName = clue.key)
                }
                break
            }
        }
    }

    //val imageList = mutableStateMapOf<Int, Uri>()

    //val cellLetterMap = mutableStateMapOf<Pair<Int,Int>,String>()
    private fun updateGridCell(cell : Cell){
        //make a new cell to update the grid with

        Log.i(TAG, "replacing ${uiState.value.currentCell} with $cell")


        //go through the current puzzle and add the new cell to all the clues
        var clue_box_idx = -1
        uiState.value.currentPuzzle.clues.forEach { (key, clue) ->
            try {
                val idx = clue.cells.indexOf(_uiState.value.currentCell)
                Log.i(TAG, "clue $key idx is: $idx")
                if (idx >= 0) {
                    clue.cells[idx] = cell
                }
                if (clue == _uiState.value.currentClue) {
                    clue_box_idx = idx
                    _uiState.value.currentClue.cells[clue_box_idx] = cell
                }
            }
            catch(exception : ArrayIndexOutOfBoundsException){
                if(clue_box_idx == -1 ) {
                    Log.w(TAG, "Tried using an Index of -1")
                    exception.message?.let { Log.w(TAG, it) }
                }
                else{
                    throw exception
                }
            }
        }

        //update the the ui's concept of the current cell
        _uiState.update { it.copy(currentCell = cell) }

    }

    fun setLetter(letter: String){
        viewModelScope.launch(Dispatchers.IO) {

            //make a new cell to update the grid with
            val newCell = Cell(
                _uiState.value.currentCell.x,
                _uiState.value.currentCell.y,
                letter
            )

            updateGridCell(newCell)

            updatePuzzleData()

            //increment active if its not the last cell in the clue
            val clue_box_idx = uiState.value.currentClue.cells.indexOf(uiState.value.currentCell)

            Log.i(TAG,"current clue index: $clue_box_idx")

                if(clue_box_idx < _uiState.value.currentClue.cells.size - 1 ) {
                    Log.i(TAG,"updating position in clue : ${clue_box_idx+1}")
                    _uiState.update { ui ->
                        ui.copy(
                            currentCell = _uiState.value.currentClue.cells[clue_box_idx+1],
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
    }


    fun delLetter(){
        viewModelScope.launch(Dispatchers.IO) {


            //make a new cell to update the grid with
            val newCell = Cell(
                _uiState.value.currentCell.x,
                _uiState.value.currentCell.y,
                ""
            )

            updateGridCell(newCell)

            // update the database with the puzzle.
            // Log.i(TAG, "calling updateDatabase")
            // updatePuzzleFile(puzzleFilePath,uiState.value.currentPuzzle)

            // repository.updatePuzzleEditTime(puzzleId)

            // Log.i(TAG, "Finished updating database")
            updatePuzzleData()

            //update the grids ui state to move the selected clue box to the next one.
            val clue_box_idx = uiState.value.currentClue.cells.indexOf(uiState.value.currentCell)
            Log.i(TAG,"current position in clue : $clue_box_idx. Clue's size ${_uiState.value.currentClue.cells.size}")
            //increment active if its not the last cell in the clue
            if(clue_box_idx > 0 ) {
                Log.i(TAG,"updating position in clue : ${clue_box_idx-1}")
                _uiState.update { ui ->
                    ui.copy(
                        currentCell = _uiState.value.currentClue.cells[clue_box_idx-1],
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

    private suspend fun updatePuzzleData(){
        Log.i(TAG, "Updating Puzzle file")
        updatePuzzleFile(puzzleFilePath,uiState.value.currentPuzzle)
        Log.i(TAG, "Updating puzzle database with last edit time")
        repository.updatePuzzleEditTime(puzzleId)
    }

}

class PuzzleSolveViewModelFactory(private val repository: PuzzleRepository, private val puzzleId : String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PuzzleSolveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PuzzleSolveViewModel(repository,puzzleId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
