package com.jhb.crosswordScan.ui.solveScreen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhb.crosswordScan.data.Cell
import com.jhb.crosswordScan.data.Clue
import com.jhb.crosswordScan.data.Puzzle
import com.jhb.crosswordScan.data.PuzzleRepository
import com.jhb.crosswordScan.data.modifyPuzzleWithCell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class PuzzleSolveViewModel(private val repository: PuzzleRepository, private val puzzleId : String ): ViewModel() {
    companion object {
        private const val TAG = "PuzzleSolveViewModel"
    }

    protected val _uiState = MutableStateFlow(PuzzleUiState())
    protected val _uiErrors = MutableStateFlow(UiErrorState())

    val uiState : StateFlow<PuzzleUiState> = _uiState
    val uiError : StateFlow<UiErrorState> = _uiErrors

    val puzzle = MutableStateFlow(Puzzle( mapOf(), mapOf()))
    val cells = mutableStateOf(listOf<Cell>())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            puzzle.collect { p ->
                Log.i(TAG,"collected new data to update ui")

                val cellList = p.cells.toList().sortedBy { it }

                _uiState.update {
                    it.copy(
                        cells = cellList,
                        gridSize = p.gridSize,
                        acrossClues = p.across.toList(),
                        downClues = p.down.toList(),
                        labeledCells = getLabelledCells(p),
                        currentClue = p.clues[it.currentClueName]?: Clue(mutableListOf(),"no data")
                    )
                }
            }
        }
    }

    open fun dispose(){

    }

    open suspend fun setUpPuzzleData(){

    }

    fun toggleCollapseKeyboard(){
        _uiState.update { ui ->
            ui.copy(
                keyboardCollapsed = !_uiState.value.keyboardCollapsed,
            )
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
        cellSetLabels.forEach({(k, v)-> Log.d(TAG, "special cells ${k.x},${k.y} with $v") })
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
                currentClue = puzzle.value.clues[name]!!,
                currentCell = puzzle.value.clues[name]!!.cells[0],
                currentClueName = name
            )
        }
    }

    fun updateActiveClueByCell(cell : Cell) {
        Log.i(TAG, "trying to update clue at $cell. " +
                    "from current Active clue ${_uiState.value.currentClue} ")
        val allClues = puzzle.value.clues
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

     protected fun updateGridCellFromUserInput(cell : Cell){
         Log.i(TAG, "replacing ${uiState.value.currentCell} with $cell")
         val newPuzzle = modifyPuzzleWithCell(puzzle=puzzle.value, cell)
         puzzle.update {
            newPuzzle
         }
    }

    open fun setLetter(letter: String){
        viewModelScope.launch(Dispatchers.IO) {

            //make a new cell to update the grid with
            val newCell = Cell(
                _uiState.value.currentCell.x,
                _uiState.value.currentCell.y,
                letter
            )

            updateGridCellFromUserInput(newCell)

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

    open fun delLetter(){
        viewModelScope.launch(Dispatchers.IO) {
            //make a new cell to update the grid with
            val newCell = Cell(
                _uiState.value.currentCell.x,
                _uiState.value.currentCell.y,
                ""
            )

            updateGridCellFromUserInput(newCell)

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

}