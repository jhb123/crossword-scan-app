package com.example.learn_opencv

class Puzzle() {

    //should be a way of stopping external object changing this?
    private val _clues = mutableMapOf<String, Clue>()
    val clues : Map<String, Clue> = _clues

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
