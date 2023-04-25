package com.example.learn_opencv.viewModels

import androidx.lifecycle.*
import com.example.learn_opencv.Clue
import com.example.learn_opencv.PuzzleData
import com.example.learn_opencv.PuzzleRepository


class puzzleViewModel(private val repository: PuzzleRepository): ViewModel() {

    val allPuzzles: LiveData<List<PuzzleData>> = repository.allPuzzles.asLiveData()

}

class puzzleViewModelFactory(private val repository: PuzzleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(puzzleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return puzzleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}