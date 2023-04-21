package com.example.learn_opencv

import androidx.lifecycle.ViewModel
import org.opencv.core.Mat
import java.util.LinkedList
import java.util.Map.entry
import kotlin.properties.Delegates

class clue(clueName : String, clueBoxes : List<Pair<Int,Int>> ){
    val clueName = clueName
    val clueBoxes = clueBoxes
}

class puzzleViewModel: ViewModel() {

    var dimension = 0  //the grids are always square, e.g. 15x15 black and white square


    private var _activeClue = "dfh"
    var activeClue: String = "afaa"
        get() = _activeClue

    private val _clue1 = clue("1d", listOf( Pair(0,0),Pair(1,0),Pair(2,0),Pair(3,0) ))
    private val _clue2 = clue("1a", listOf( Pair(0,0),Pair(0,1),Pair(0,2),Pair(0,3) ))
    private val _clue3 = clue("3a", listOf( Pair(2,0),Pair(2,1),Pair(2,2) ))
    private val _clue4 = clue("2d" , listOf( Pair(0,2),Pair(1,2),Pair(2,2) ))

    //for getting cells by clue
    private val _clues = mapOf(_clue1.clueName to _clue1,
        _clue2.clueName to _clue2,
        _clue3.clueName to _clue3,
        _clue4.clueName to _clue4)

    // this is a map where the keys are coordinates and the values are clues associated with the
    // coordinate. this gets clue by cell
    private val _clueMap = createClueMap(_clues)


    private fun createClueMap(clues : Map<String, clue>) : MutableMap< Pair<Int, Int>, MutableList<String>> {
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

    val clues: Map<String, clue>
        get() = _clues

    val clueMap: MutableMap< Pair<Int, Int>, MutableList<String>>
        get() = _clueMap

}