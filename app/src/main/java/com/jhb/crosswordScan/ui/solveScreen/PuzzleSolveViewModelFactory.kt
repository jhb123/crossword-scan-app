package com.jhb.crosswordScan.ui.solveScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jhb.crosswordScan.data.PuzzleRepository

class PuzzleSolveViewModelFactory(private val repository: PuzzleRepository, private val puzzleId : String, private val remote: Boolean) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (remote) {
            RemotePuzzleSolveViewModel(repository,puzzleId) as T
        } else {
            LocalPuzzleSolveViewModel(repository,puzzleId) as T
        }
    }
}