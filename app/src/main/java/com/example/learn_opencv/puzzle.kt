package com.example.learn_opencv

import android.graphics.Bitmap
import androidx.room.TypeConverter
import com.google.gson.Gson

class Puzzle() {

    //should be a way of stopping external object changing this?
    var clues = mutableMapOf<String, Clue>()

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
