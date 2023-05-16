package com.example.learn_opencv.viewModels

import androidx.lifecycle.*
import com.example.learn_opencv.data.PuzzleRepository

class PuzzleSelectViewModel(private val repository: PuzzleRepository): ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
    val allPuzzles = repository.allPuzzles.asLiveData()

    //private val _uiState = MutableStateFlow(PuzzleUiState())
    //val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow() //backing property

    //val allPuzzles: LiveData<List<PuzzleData>> = repository.allPuzzles.asLiveData()

//    val homeUiState: StateFlow<HomeUiState> =
//        itemsRepository.getAllItemsStream()

}

class PuzzleSelectViewModelFactory(private val repository: PuzzleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PuzzleSelectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PuzzleSelectViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//class PuzzleSolveViewModel(private val repository: PuzzleRepository ): ViewModel() {
//
//    private val TAG = "PuzzleSolveViewModel"
//    val puzzleData = repository.getPuzzle.asLiveData()
//
//
//    //val allPuzzles: LiveData<List<PuzzleData>> = repository.allPuzzles.asLiveData()
//
//    lateinit var clues : Map<String, Clue> //connect an observer to this?
//
//    var dimension = 0  //the grids are always square, e.g. 15x15 black and white square
//
//    private var _activeClue = "dfh" //some random default string that probably doesn't need to be here
//    var activeClue: String = "afaa"
//        get() = _activeClue
//
//
//    //this is a set of the coordinates in the grid.
//    lateinit var coordSet : Set<Pair<Int, Int>>//createCoordSet(clues)
////    var coordSet: Set<Pair<Int, Int>>
////        get() = _coordSet
////        set(value) {_coordSet = value}
//
//    //this is a map of the coordinates in the grid and the text at that coordinate
//    lateinit var coordTextMap :  MutableMap< Pair<Int, Int>, MutableLiveData<String>> //createCoordTextMap(clues)
////    val coordTextMap : MutableMap< Pair<Int, Int>, MutableLiveData<String>>
////        get() = _coordTextMap
//
//
//    // this is a map where the keys are coordinates and the values are a list strings of the names
//    // of the clues associated with the coordinate. this gets Clue by cell
//    lateinit var coordClueNamesMap : MutableMap< Pair<Int, Int>, MutableList<String>> //createCoordClueNamesMap(clues)
////    val coordClueNamesMap: MutableMap< Pair<Int, Int>, MutableList<String>>
////        get() = _coordClueNamesMap
//    //set(value) {_coordClueNamesMap[value.first] = value.second}
//
//    //
//
//    fun setUpData()  {
////         launch {
//        Log.i(TAG, "Setting up data")
//        coordClueNamesMap = createCoordClueNamesMap(clues)
//        coordTextMap = createCoordTextMap(clues)
//        coordSet = createCoordSet(clues)
////         }
//    }
//
//
//    private val _coordClueLabels = mapOf<Pair<Int,Int>, String>(
//        Pair(0,0) to "1" , Pair(0,2) to "3" , Pair(2,0) to "2"
//    )
//    val coordClueLabels : Map< Pair<Int,Int>, String >
//        get() = _coordClueLabels
//
//
//    private fun createCoordClueNamesMap(clues : Map<String, Clue>) : MutableMap< Pair<Int, Int>, MutableList<String>> {
//        val clueMap = mutableMapOf<Pair<Int,Int>, MutableList< String >>()
//
//        clues.forEach{entry->
//            entry.value.clueBoxes.forEach{
//                if( it in clueMap){
//                    clueMap[it]!!.add(entry.value.clueName)
//                }
//                else{
//                    clueMap[it] = mutableListOf(entry.value.clueName)
//                }
//            }
//
//        }
//        return clueMap
//    }
//
//    private fun createCoordSet(clues : Map<String, Clue>): Set<Pair<Int, Int>>{
//        val clueSet = mutableSetOf<Pair<Int,Int>>()
//        clues.forEach { (s, clue) ->
//            clue.clueBoxes.forEach{
//                clueSet.add(it)
//            }
//        }
//        return clueSet
//    }
//
//    private fun createCoordTextMap(clues : Map<String, Clue>) : MutableMap< Pair<Int, Int>, MutableLiveData<String>> {
//        val coordTextMap = mutableMapOf<Pair<Int,Int>, MutableLiveData<String>>()
//        coordSet.forEach{
//            coordTextMap[it] = MutableLiveData<String>("") //start each blank?
//        }
//        return coordTextMap
//    }
//}
//class PuzzleSolveViewModelFactory(private val repository: PuzzleRepository) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        Log.i("CrosswordScanViewModelFactory","Creating")
//        if (modelClass.isAssignableFrom(PuzzleSolveViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return PuzzleSolveViewModel(repository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}

//I imagine this could be stored in a json fairly easily. For now we have this dummy data.
//    private val _clue1 = Clue("1d", listOf( Pair(0,0),Pair(1,0),Pair(2,0),Pair(3,0) ))
//    private val _clue2 = Clue("1a", listOf( Pair(0,0),Pair(0,1),Pair(0,2),Pair(0,3) ))
//    private val _clue3 = Clue("3a", listOf( Pair(2,0),Pair(2,1),Pair(2,2) ))
//    private val _clue4 = Clue("2d", listOf( Pair(0,2),Pair(1,2),Pair(2,2) ))
//    private val _clue5 = Clue("4a", listOf( Pair(4,1),Pair(4,2),Pair(4,3) ))
//    private val _clue6 = Clue("5a", listOf( Pair(6,0),Pair(6,1),Pair(6,2) ))


//for getting cells by Clue. This is a map of clues, the key is the Clue's name and the
//value is a list of coordinates
//    private val _clues = mapOf(_clue1.clueName to _clue1,
//        _clue2.clueName to _clue2,
//        _clue3.clueName to _clue3,
//        _clue4.clueName to _clue4,
//        _clue5.clueName to _clue5,
//        _clue6.clueName to _clue6)
