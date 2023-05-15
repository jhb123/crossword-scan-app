package com.example.learn_opencv

import android.util.Log

private const val TAG = "puzzle"

class Puzzle() {

    //should be a way of stopping external object changing this?
    private val _clues = mutableMapOf<String, Clue>()
    val clues : Map<String, Clue> = _clues

    fun updateClueTxt(name: String, newtxt : String){
        if(name in _clues){
            _clues[name]!!.clue = newtxt //hopefully this doesn't break because of the !!
        }
        else{
            Log.w(TAG,"Clue $name not in puzzle, so $newtxt skipped ")
        }
    }

    //var image = Bitmap()

    fun addClue(name:String, clue: Clue){
        if(name in _clues){
            throw Exception("A clue with this name already exists")
        }
        else{
            _clues[name]= clue
        }

    }

}
