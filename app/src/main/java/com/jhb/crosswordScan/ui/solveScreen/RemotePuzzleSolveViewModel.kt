package com.jhb.crosswordScan.ui.solveScreen

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.jhb.crosswordScan.data.Cell
import com.jhb.crosswordScan.data.PuzzleFromJson
import com.jhb.crosswordScan.data.PuzzleRepository
import com.jhb.crosswordScan.data.modifyPuzzleWithCell
import com.jhb.crosswordScan.network.CrosswordApi.retrofitService
import com.jhb.crosswordScan.network.CrosswordWebSocketClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemotePuzzleSolveViewModel(private val repository: PuzzleRepository, private val puzzleId : String ):
    PuzzleSolveViewModel(repository, puzzleId)  {

    companion object {
        private const val TAG = "RemotePuzzleSolveViewModel"
    }

    lateinit var webSocketClient: CrosswordWebSocketClient

    private var _cellUpates = MutableLiveData<Cell>()
    val cellObserver = Observer<Cell> { cell ->
        val newPuzzle = modifyPuzzleWithCell(puzzle=puzzle.value, cell)
        puzzle.update {
            newPuzzle
        }
        if (cell.x == uiState.value.currentCell.x && cell.y == uiState.value.currentCell.y) {
            _uiState.update { it.copy(currentCell = cell) }
        }
    }

    init {
        Log.i(TAG,"initialising ui")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val puzzleData = repository.getPuzzle(puzzleId)
                Log.i(TAG, "puzzleData id ${puzzleData.id}")

                puzzleData.serverId?.let {

                    val response = retrofitService.getPuzzleData(it)

                    puzzle.update { PuzzleFromJson(response.string()) }

                    webSocketClient = CrosswordWebSocketClient(it)
                }
            }
            withContext(Dispatchers.Main) {
                _cellUpates = webSocketClient.cellUpdates
                _cellUpates.observeForever(cellObserver)
            }
        }

        Log.i(TAG,"Finished initialising ui")
    }

    override fun setLetter(letter: String){
        super.setLetter(letter)
        val newCell = Cell(
            _uiState.value.currentCell.x,
            _uiState.value.currentCell.y,
            letter
        )
        webSocketClient.ws.send(newCell.toString())
    }

    override fun delLetter(){
        super.delLetter()
        val newCell = Cell(
            _uiState.value.currentCell.x,
            _uiState.value.currentCell.y,
            " "
        )
        webSocketClient.ws.send(newCell.toString())
    }



//    override suspend fun updatePuzzleData() {
//        webSocketClient.ws.send()
//    }
}