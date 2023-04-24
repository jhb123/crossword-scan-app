package com.example.learn_opencv

import android.graphics.Bitmap

class Puzzle() {

    //should be a way of stopping external object changing this?
    val clues = mutableMapOf<String, Clue>()

    //var image = Bitmap()

    fun addClue(name:String, clue: Clue){
        if(name in clues){
            throw Exception("A clue with this name already exists")
        }
        else{
            clues[name]= clue
        }

    }


}