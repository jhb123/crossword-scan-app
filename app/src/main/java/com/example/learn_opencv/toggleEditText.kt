package com.example.learn_opencv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.BLACK
import android.graphics.Paint
import android.text.TextPaint
import android.util.Log
import android.view.KeyEvent

enum class toggleState {NOT_SELECT, SELECT_A, SELECT_B}

class toggleEditText (context: Context) :
    androidx.appcompat.widget.AppCompatEditText(context) {
    private val TAG = "toggleEditText"
    var toggled = toggleState.NOT_SELECT
    var isCheckLetter = false
    var activeClueName = ""
    var row: Int = -1
    var col: Int = -1

//    override fun performClick(): Boolean {
//        Log.i(TAG,"Clicked (${row},${col})")
//        setSelection(0)
//        return false// super.performClick()
//    }
//    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
//    Log.i(TAG,"keyCode $keyCode, event $event")
//        return super.onKeyDown(keyCode, event)
//    }


    }



