package com.jhb.crosswordScan.ui.solveScreen

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.jhb.crosswordScan.data.PuzzleRepository
import com.jhb.crosswordScan.data.readFileAsPuzzle
import com.jhb.crosswordScan.data.updatePuzzleFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalPuzzleSolveViewModel(private val repository: PuzzleRepository, private val puzzleId : String ):
    PuzzleSolveViewModel(repository, puzzleId) {

    companion object {
        private const val TAG = "LocalPuzzleSolveViewModel"
    }
    lateinit var puzzleFilePath : String

    init {
        Log.i(TAG,"initialising ui")

        viewModelScope.launch {
            //delay(50)
            withContext(Dispatchers.IO) {
                val puzzleData = repository.getPuzzle(puzzleId)
                Log.i(TAG, "Collecting puzzle from database")
                Log.i(TAG, "puzzleData puzzle ${puzzleData.file}")
                Log.i(TAG, "puzzleData id ${puzzleData.id}")
                puzzleData?.file?.let {
                    val localPuzzle = readFileAsPuzzle(it)
                    puzzleFilePath = it

                    puzzle.update { localPuzzle!! }

                    _uiState.update {
                        it.copy(
                            puzzleId = puzzleData.id,
                        )
                    }
                }
            }
        }
        Log.i(TAG,"Finished initialising ui")
    }

    override fun setLetter(letter: String){
        super.setLetter(letter)
        updatePuzzleData()
    }

    override fun delLetter(){
        super.delLetter()
        updatePuzzleData()
    }

    private fun updatePuzzleData(){
        Log.i(TAG, "Updating Puzzle file")
        viewModelScope.launch(Dispatchers.IO) {
            updatePuzzleFile(puzzleFilePath, puzzle.value)
        }
    }
}
