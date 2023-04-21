package com.example.learn_opencv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.BLACK
import android.graphics.Paint
import android.text.TextPaint

enum class toggleState {NOT_SELECT, SELECT_A, SELECT_B}

class toggleEditText (context: Context) :
    androidx.appcompat.widget.AppCompatEditText(context) {
    private val TAG = "toggleEditText"
    var toggled = toggleState.NOT_SELECT
    var isCheckLetter = false
    var activeClueName = ""
    var row: Int = -1
    var col: Int = -1


    }



