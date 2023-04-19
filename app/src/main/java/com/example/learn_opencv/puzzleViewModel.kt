package com.example.learn_opencv

import androidx.lifecycle.ViewModel
import org.opencv.core.Mat
import java.util.LinkedList
import kotlin.properties.Delegates

class puzzleViewModel: ViewModel() {

    var dimension = 0  //the grids are always square, e.g. 15x15 black and white square


    private val _clue1 = listOf<Pair<Int,Int>>( Pair(0,0),Pair(1,0),Pair(2,0) )
    private val _clue2 = listOf<Pair<Int,Int>>( Pair(0,0),Pair(0,1),Pair(0,2) )
    private val _clue3 = listOf<Pair<Int,Int>>( Pair(2,0),Pair(2,1),Pair(2,2) )
    private val _clue4 = listOf<Pair<Int,Int>>( Pair(0,2),Pair(1,2),Pair(2,2) )


    private val _clues = listOf<List<Pair<Int,Int>>>(_clue1,_clue2,_clue3,_clue4)

    // this is a map where the keys are coordinates and the values are clues associated with the
    // coordinate
    private val _clueMap = createClueMap(_clues)


//    for( clue in _clues){
//        for(box in clue){
//            _clueMap[box] = clue
//
//        }
//    }
    private fun createClueMap(clues : List<List<Pair<Int, Int>>>): MutableMap<Pair<Int, Int>, MutableList<List<Pair<Int, Int>>>> {
        val clueMap = mutableMapOf<Pair<Int,Int>, MutableList< List<Pair<Int,Int>>>>()
        //should make this only call once?
        for( clue in clues){
            for(box in clue){
                if( box in clueMap){
                    clueMap[box]!!.add(clue)
                }
                else{
                    clueMap[box] = mutableListOf(clue)
                }
            }
        }
        return clueMap
    }

    val clues: List<List<Pair<Int,Int>>>
        get() = _clues

    val clueMap: MutableMap<Pair<Int, Int>, MutableList<List<Pair<Int, Int>>>>
        get() = _clueMap

}