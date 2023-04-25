package com.example.learn_opencv.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learn_opencv.Clue
import com.example.learn_opencv.Puzzle

class PuzzleSolveViewModel(): ViewModel() {

    var dimension = 0  //the grids are always square, e.g. 15x15 black and white square

    private var _activeClue = "dfh"
    var activeClue: String = "afaa"
        get() = _activeClue

    //I imagine this could be stored in a json fairly easily. For now we have this dummy data.
    private val _clue1 = Clue("1d", listOf( Pair(0,0),Pair(1,0),Pair(2,0),Pair(3,0) ))
    private val _clue2 = Clue("1a", listOf( Pair(0,0),Pair(0,1),Pair(0,2),Pair(0,3) ))
    private val _clue3 = Clue("3a", listOf( Pair(2,0),Pair(2,1),Pair(2,2) ))
    private val _clue4 = Clue("2d", listOf( Pair(0,2),Pair(1,2),Pair(2,2) ))
    private val _clue5 = Clue("4a", listOf( Pair(4,1),Pair(4,2),Pair(4,3) ))
    private val _clue6 = Clue("5a", listOf( Pair(6,0),Pair(6,1),Pair(6,2) ))


    //for getting cells by Clue. This is a map of clues, the key is the Clue's name and the
    //value is a list of coordinates
    private val _clues = mapOf(_clue1.clueName to _clue1,
        _clue2.clueName to _clue2,
        _clue3.clueName to _clue3,
        _clue4.clueName to _clue4,
        _clue5.clueName to _clue5,
        _clue6.clueName to _clue6)

    val clues: Map<String, Clue>
        get() = _clues

    //this is a set of the coordinates in the grid.
    private val _coordSet = createCoordSet(_clues)
    val coordSet: Set<Pair<Int, Int>>
        get() = _coordSet

    //this is a map of the coordinates in the grid and the text at that coordinate
    private val _coordTextMap = createCoordTextMap(_clues)
    val coordTextMap : MutableMap< Pair<Int, Int>, MutableLiveData<String>>
        get() = _coordTextMap


    // this is a map where the keys are coordinates and the values are a list strings of the names
    // of the clues associated with the coordinate. this gets Clue by cell
    private val _coordClueNamesMap = createCoordClueNamesMap(_clues)
    val coordClueNamesMap: MutableMap< Pair<Int, Int>, MutableList<String>>
        get() = _coordClueNamesMap
    //set(value) {_coordClueNamesMap[value.first] = value.second}

    //
    private val _coordClueLabels = mapOf<Pair<Int,Int>, String>(
        Pair(0,0) to "1" , Pair(0,2) to "3" , Pair(2,0) to "2"
    )
    val coordClueLabels : Map< Pair<Int,Int>, String >
        get() = _coordClueLabels


    private fun createCoordClueNamesMap(clues : Map<String, Clue>) : MutableMap< Pair<Int, Int>, MutableList<String>> {
        val clueMap = mutableMapOf<Pair<Int,Int>, MutableList< String >>()

        clues.forEach{entry->
            entry.value.clueBoxes.forEach{
                if( it in clueMap){
                    clueMap[it]!!.add(entry.value.clueName)
                }
                else{
                    clueMap[it] = mutableListOf(entry.value.clueName)
                }
            }

        }
        return clueMap
    }

    private fun createCoordSet(clues : Map<String, Clue>): Set<Pair<Int, Int>>{
        val clueSet = mutableSetOf<Pair<Int,Int>>()
        clues.forEach { (s, clue) ->
            clue.clueBoxes.forEach{
                clueSet.add(it)
            }
        }
        return clueSet
    }

    private fun createCoordTextMap(clues : Map<String, Clue>) : MutableMap< Pair<Int, Int>, MutableLiveData<String>> {
        val coordTextMap = mutableMapOf<Pair<Int,Int>, MutableLiveData<String>>()
        _coordSet.forEach{
            coordTextMap[it] = MutableLiveData<String>("") //start each blank?
        }
        return coordTextMap
    }
}