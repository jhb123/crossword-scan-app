package com.jhb.crosswordScan.ui.common

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jhb.crosswordScan.data.Clue

@Composable
fun clueTextBox(clueData : Pair<String, String>,
                backgroundColor: Color = MaterialTheme.colorScheme.surface,
                textColor: Color = MaterialTheme.colorScheme.onSurface) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,//MaterialTheme.colorScheme.surface,
        ),
        //shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(170.dp)
            .padding(5.dp)

    ) {

//      Box(
//        modifier = Modifier
//            //.background(color = backgroundColor)
//            .padding(5.dp)
//            .width(170.dp)
//            .background(color = backgroundColor, shape = RoundedCornerShape(5.dp))
//    ){
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

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,//MaterialTheme.colorScheme.surface,
        ),
        //shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(170.dp)
            .padding(5.dp)
            .pointerInput(clueData.second.clueName) {
                detectTapGestures {
                    onClueSelect(clueData.second.clueName)
                }
            }
    ){

//    Box(
//        modifier = Modifier
//            //.background(color = backgroundColor)
//            .padding(5.dp)
//            .width(170.dp)
//            .background(color = backgroundColor, shape = RoundedCornerShape(5.dp))
//            .pointerInput(clueData.second.clueName) {
//                detectTapGestures {
//                    onClueSelect(clueData.second.clueName)
//                }
//            }
//    ){

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