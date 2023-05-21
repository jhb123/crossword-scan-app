package com.jhb.crosswordScan.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jhb.crosswordScan.data.Clue

@Composable
fun clueTextBox(clueData : Pair<String, String>,
                backgroundColor: Color,
                textColor: Color) {
    Box(
        modifier = Modifier
            //.background(color = backgroundColor)
            .padding(5.dp)
            .width(170.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(5.dp))
    ){
        Text(
            "${clueData.first}) ${clueData.second}",
            color = textColor,
            modifier = Modifier
                .padding(5.dp)
        )
    }
}


@Composable
fun dynamicClueTextBox(clueData : Pair<String, Clue>,
                backgroundColor: Color,
                textColor: Color,
                onClueSelect: (String) -> Unit ) {
    Box(
        modifier = Modifier
            //.background(color = backgroundColor)
            .padding(5.dp)
            .width(170.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(5.dp))
            .pointerInput(clueData.second.clueName) {
                detectTapGestures {
                    onClueSelect(clueData.second.clueName)
                }
            }
    ){

        Text(
            "${clueData.first}) ${clueData.second.clue}",
            color = textColor,
            //shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                //.fillMaxWidth(0.5f)
                .padding(5.dp)

            //.clip(RoundedCornerShape(20.dp))
            //shape = RoundedCornerShape(20.dp)
        )
    }
}