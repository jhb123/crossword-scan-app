package com.jhb.crosswordScan.ui.solveScreen

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.jhb.crosswordScan.data.Cell
import com.jhb.crosswordScan.data.PuzzleFromJson
import com.jhb.crosswordScan.data.PuzzleRepository
import com.jhb.crosswordScan.data.modifyPuzzleWithCell
import com.jhb.crosswordScan.network.ConnectionStatus
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

    var webSocketClient: CrosswordWebSocketClient? = null

    private var cellUpates = MutableLiveData<Cell>()
    private var websocketConnectionStatus = MutableLiveData<ConnectionStatus>()


    val cellObserver = Observer<Cell> { cell ->
        val newPuzzle = modifyPuzzleWithCell(puzzle=puzzle.value, cell)
        puzzle.update {
            newPuzzle
        }
        if (cell.x == uiState.value.currentCell.x && cell.y == uiState.value.currentCell.y) {
            _uiState.update { it.copy(currentCell = cell) }
        }
    }

    val connectionStateObserver = Observer<ConnectionStatus> { status ->
        when (status) {
            ConnectionStatus.Connected -> _uiErrors.update {
                it.copy(error = null)
            }
            ConnectionStatus.Disconnected -> _uiErrors.update {
                it.copy(error = Error.WebsocketError())
            }
        }
    }

//    init {
//        Log.i(TAG,"initialising ui")
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                connectToPuzzle()
//            }
//        }
//
//        Log.i(TAG,"Finished initialising ui")
//    }

    override suspend fun setUpPuzzleData() {
        withContext(Dispatchers.IO) {
            connectToPuzzle()
        }
    }

    override fun dispose(){
        webSocketClient?.ws?.close(1001,"Finished Puzzle")
    }

    private suspend fun connectToPuzzle() {
        val puzzleData = repository.getPuzzle(puzzleId)
        Log.i(TAG, "puzzleData id ${puzzleData.id}")

        puzzleData.serverId?.let {

            val response = retrofitService.getPuzzleData(it)

            puzzle.update { PuzzleFromJson(response.string()) }

            webSocketClient = CrosswordWebSocketClient(it)
            SetUpObservers()
        }
    }

    fun SetUpObservers(){
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                 webSocketClient?.also {
                     cellUpates = it.cellUpdates
                     cellUpates.observeForever(cellObserver)

                     websocketConnectionStatus = it.connectionStatus
                     websocketConnectionStatus.observeForever(connectionStateObserver)

                 }
            }
        }
    }

    fun setOnlineState(isOnline: Boolean){
        if (isOnline) {
            when (websocketConnectionStatus.value) {
                ConnectionStatus.Connected -> {
                    _uiErrors.update {
                        it.copy(error = null)
                    }
                }
                ConnectionStatus.Disconnected -> {
                    _uiErrors.update {
                        it.copy(error = Error.WebsocketError())
                    }
                }
                null -> {
                    _uiErrors.update {
                        it.copy(error = Error.WebsocketError())
                    }
                }
            }
        } else {
            _uiErrors.update {
                it.copy(error = Error.NetworkError())
            }
        }
    }

    override fun setLetter(letter: String){
        super.setLetter(letter)
        val newCell = Cell(
            _uiState.value.currentCell.x,
            _uiState.value.currentCell.y,
            letter
        )
        sendWebsocketUpdate(newCell)

    }

    override fun delLetter(){
        super.delLetter()
        val newCell = Cell(
            _uiState.value.currentCell.x,
            _uiState.value.currentCell.y,
            " "
        )
        sendWebsocketUpdate(newCell)

    }

    private fun sendWebsocketUpdate(newCell: Cell) {
        webSocketClient?.ws?.send(newCell.toString())?.also {
            if (!it) {
                _uiErrors.update {
                    it.copy(error = Error.WebsocketError())
                }
            }
        } ?: run {
            // Action to perform when sent is null
            _uiErrors.update { state ->
                state.copy(error = Error.WebsocketError())
            }
        }
    }

}