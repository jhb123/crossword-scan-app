package com.example.learn_opencv

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText

enum class toggleState {NOT_SELECT, SELECT_A, SELECT_B}

class toggleEditText (context: Context) :
    androidx.appcompat.widget.AppCompatEditText(context) {

        var toggled = toggleState.NOT_SELECT
        var row: Int = -1
        var col: Int = -1

}

