package com.example.learn_opencv.data

class Clue(clueName : String, clueBoxes : MutableList<Triple<Int,Int,String>> ){
    val clueName = clueName
    val clueBoxes = clueBoxes
    var clue = ""
}