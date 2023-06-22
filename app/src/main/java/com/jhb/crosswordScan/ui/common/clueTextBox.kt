package com.jhb.crosswordScan.ui.common

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
        modifier = Modifier
            .width(170.dp)
            .padding(5.dp)

    ) {
        Text(
            "${clueData.first}) ${clueData.second}",
            color = textColor,
            modifier = Modifier
                .padding(5.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableClueTextBox(
    clueData : Pair<String, String>,
    colors: CardColors = CardDefaults.cardColors(),
    onClick : () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = colors,
        onClick = { onClick() },
        modifier = modifier
    ) {
        Text(
            "${clueData.first}) ${clueData.second}",
            modifier = Modifier
                .padding(5.dp)
        )
    }
}




@Composable
fun dynamicClueTextBox(clueData : Pair<String, Clue>,
                backgroundColor: Color,
                textColor: Color,
                onClueSelect: (String) -> Unit,
                modifier: Modifier = Modifier
) {

    // this composable is used in cases where a function needs to be
    // be applied based on the text in the clue box.

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,//MaterialTheme.colorScheme.surface,
        ),
        //shape = RoundedCornerShape(10.dp),
        modifier = modifier
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
