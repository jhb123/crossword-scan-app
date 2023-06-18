package com.jhb.crosswordScan.ui.solveScreen

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.jhb.crosswordScan.data.*
import com.jhb.crosswordScan.network.CrosswordApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.MediaType
import okhttp3.RequestBody
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


    fun convertPuzzleToCellSet(puzzle : Puzzle) : MutableSet<Triple<Int, Int, String>>{
        val cellSet = mutableSetOf<Triple<Int, Int, String>>()
        puzzle.clues.forEach { s, clue ->
            clue.clueBoxes.forEach {
                cellSet.add(it)
            }
        }
        return cellSet
    }

    fun cloudSync(){

        viewModelScope.launch {

            //uploadPuzzle()
            //update the local puzzle
            //updatePuzzleFile(puzzleFilePath,uiState.value.currentPuzzle)
        }

        // step1, pass the puzzle to the server.
        // Use the servers response as the new grid.
    }

    private fun continuousUpdate(){
        // keep calling the sync puzzle function while you're in the puzzle.
        // do this every 0.5s or something.
    }

    private suspend fun syncPuzzle(){
        //do this immediately when opening the puzzle to check for the latest version.

        //check if the puzzle needs syncing by looking at its last modified time

        // if the server's puzzle has been updated more recently than the local version
        // get the newest version and replace the local version with it

        //if the client's puzzle has been updated more recently than the server's version
        // then upload the clients puzzle to the server
    }

    private suspend fun checkServerPuzzleUpate(){
        // obtain the lastmodified time of the current puzzle.

    }

    private suspend fun uploadPuzzle(){
        val puzzlePayload = mapOf(
            "id" to _uiState.value.puzzleId,
            "puzzle" to _uiState.value.currentPuzzle
        )
        val payload = Gson().toJson(puzzlePayload)
        val requestBody = RequestBody.create(MediaType.get("application/json"), payload)
        if( Session.sessionDataState.value != null ) {
            val Authorization = "Bearer ${Session.sessionDataState.value?.token}"
            Log.i(TAG,"uploading with $Authorization")
            val message = CrosswordApi.retrofitService.upload( Authorization,requestBody)
            Log.i(TAG,message.string())
        }
    }

    private fun getGuid(){
        viewModelScope.launch {
            val guid = CrosswordApi.retrofitService.getGuid()

        }
    }

    fun getLabelledCells(puzzle : Puzzle) : Map<Triple<Int, Int, String>,String > {
        val cellSetLabels = mutableMapOf< Triple<Int, Int, String>,String >()
        puzzle.clues.forEach { s, clue ->

            val regex = Regex("\\d+")
            val processed = regex.find(s)
            if (processed != null) {
                cellSetLabels[clue.clueBoxes[0]] = processed.value
            }
            else{
                Log.w(TAG,"Badly labelled clue: $s")
            }
        }
        return cellSetLabels
    }

    fun updateCurrentCell(cell : Triple<Int,Int,String>){
        _uiState.update { ui ->
            ui.copy(currentCell = cell)
        }
    }

    fun updateactiveClue(name : String){
        _uiState.update { ui->
            ui.copy(
                currentClue = _uiState.value.currentPuzzle.clues[name]!!,
                currentCell = _uiState.value.currentPuzzle.clues[name]!!.clueBoxes[0]
            )
        }
    }

    fun updateactiveClue2(cell : Triple<Int,Int,String>) {
        Log.i(
            TAG, "trying to update clue at $cell. " +
                "from current Active clue ${_uiState.value.currentClue.clueName} ")
        val allClues = uiState.value.currentPuzzle.clues
        for (clue in allClues) {
            if (clue.value.clueBoxes.contains(cell) && clue.value != _uiState.value.currentClue) {
                Log.i(TAG, "setting active clue at $cell to ${clue.key}")
                _uiState.update { ui ->
                    //if ui.currentClue !=
                    ui.copy(currentClue = clue.value)
                }
                break
            }
        }
    }

    //val imageList = mutableStateMapOf<Int, Uri>()

    //val cellLetterMap = mutableStateMapOf<Pair<Int,Int>,String>()
    private fun updateGridCell(cell : Triple<Int,Int,String>){
        //make a new cell to update the grid with

        Log.i(TAG, "replacing ${uiState.value.currentCell} with $cell")


        //go through the current puzzle and add the new cell to all the clues
        var clue_box_idx = -1
        uiState.value.currentPuzzle.clues.forEach { (key, clue) ->
            try {
                val idx = clue.clueBoxes.indexOf(_uiState.value.currentCell)
                Log.i(TAG, "clue ${clue.clueName} idx is: $idx")
                if (idx >= 0) {
                    clue.clueBoxes[idx] = cell
                }
                if (clue == _uiState.value.currentClue) {
                    clue_box_idx = idx
                    _uiState.value.currentClue.clueBoxes[clue_box_idx] = cell
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
            val newCell = Triple(
                _uiState.value.currentCell.first,
                _uiState.value.currentCell.second,
                letter
            )

            updateGridCell(newCell)

            //update the database with the puzzle.
            Log.i(TAG, "calling updateDatabase")
            // TODO fix this bit
            //viewModelScope.launch {
            updatePuzzleFile(puzzleFilePath,uiState.value.currentPuzzle)
            //}
            Log.i(TAG, "Finished updating database")

            repository.updatePuzzleEditTime(puzzleId)

            //increment active if its not the last cell in the clue
            val clue_box_idx = uiState.value.currentClue.clueBoxes.indexOf(uiState.value.currentCell)

            Log.i(TAG,"current clue index: $clue_box_idx")

                if(clue_box_idx < _uiState.value.currentClue.clueBoxes.size - 1 ) {
                    Log.i(TAG,"updating position in clue : ${clue_box_idx+1}")
                    _uiState.update { ui ->
                        ui.copy(
                            currentCell = _uiState.value.currentClue.clueBoxes[clue_box_idx+1],
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
            val newCell = Triple(
                _uiState.value.currentCell.first,
                _uiState.value.currentCell.second,
                ""
            )

            updateGridCell(newCell)

            //update the database with the puzzle.
            Log.i(TAG, "calling updateDatabase")
            updatePuzzleFile(puzzleFilePath,uiState.value.currentPuzzle)

            repository.updatePuzzleEditTime(puzzleId)

            Log.i(TAG, "Finished updatingdatabase")

            val clue_box_idx = uiState.value.currentClue.clueBoxes.indexOf(uiState.value.currentCell)

            Log.i(TAG,"current position in clue : $clue_box_idx. Clue's size ${_uiState.value.currentClue.clueBoxes.size}")
            //increment active if its not the last cell in the clue
            if(clue_box_idx > 0 ) {
                Log.i(TAG,"updating position in clue : ${clue_box_idx-1}")
                _uiState.update { ui ->
                    ui.copy(
                        currentCell = _uiState.value.currentClue.clueBoxes[clue_box_idx-1],
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
