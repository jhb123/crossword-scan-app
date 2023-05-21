package com.jhb.crosswordScan.ui.clueScanScreen

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.jhb.crosswordScan.ui.common.ClueDirection
import com.jhb.crosswordScan.ui.common.ScanUiState
import com.jhb.crosswordScan.ui.common.clueTextBox
import kotlin.math.max
import kotlin.math.min

private const val TAG = "clueScanComposable"


@Composable
fun ClueScanScreen(
    uiState: State<ScanUiState>,
    setClueScanDirection : (ClueDirection) -> Unit,
    takeImage : () -> Unit,
    onDragStart : (Offset) -> Offset,
    onDrag : (PointerInputChange) -> PointerInputChange,
    setCanvasSize : (Size) -> Unit,
    scanClues : () -> Unit,
    //points: () -> List<Offset>
)
{

    val cluePicDebug = uiState.value.cluePicDebug
    val canvasOffset = uiState.value.canvasOffset
    uiState.value.croppedCluePic
    uiState.value.clueScanDirection
    uiState.value.gridPic
    uiState.value.canvasSize
    uiState.value.isScrollingCanvas
    //val points = remember{ uiState.value.selectedPoints }

    val editAreaWidth = 300
    val editAreaHeight = 400
    Log.i(TAG,"edit area dp: $editAreaWidth x $editAreaHeight")

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background))
    {

        Row(
            modifier = Modifier
                .width(editAreaWidth.dp)
                .height(editAreaHeight.dp)){
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.inverseSurface)
                .clip(RectangleShape)
            ) {
                cluePicDebug?.asImageBitmap()?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "Image of Clues",
                        contentScale = ContentScale.Fit,
                    )
                }
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onDragStart(it) },
                            onDrag = { change, _ -> onDrag(change) }
                        )
                    }
                ) {
                    setCanvasSize(this.size)

                    //Text("Canvas Size ${uiState.value.canvasSize}")

                    if(uiState.value.selectedPoints.isNotEmpty()){
                        val x1 = min(uiState.value.selectedPoints.first().x,uiState.value.selectedPoints.last().x)
                        val y1 = min(uiState.value.selectedPoints.first().y,uiState.value.selectedPoints.last().y)
                        val x2 = max(uiState.value.selectedPoints.first().x,uiState.value.selectedPoints.last().x)
                        val y2 = max(uiState.value.selectedPoints.toList().first().y,uiState.value.selectedPoints.last().y)

                        val rectPaint = Paint()
                        rectPaint.style = Paint.Style.FILL
                        rectPaint.color = Color.WHITE
                        val outlinePaint = Paint()
                        outlinePaint.style = Paint.Style.STROKE
                        outlinePaint.color = Color.WHITE
                        outlinePaint.strokeWidth = 4f

                        drawRect(
                            topLeft = Offset(x1, y1),
                            size = Size((x2 - x1), (y2 - y1)),
                            alpha = 0.3f,
                            color = androidx.compose.ui.graphics.Color.White,
                        )
                        drawRect(
                            topLeft = Offset(x1, y1),
                            size = Size((x2 - x1), (y2 - y1)),
                            style = Stroke(width = 3.dp.toPx()),
                            color = androidx.compose.ui.graphics.Color.White,
                            )
                        Log.i(TAG, "rectangle drawn using x1: $x1, x2: $x2, y1: $y1, y2: $y2")
                        Log.i(TAG,"rectangle offset at ${Offset(x1, y1)}")
                        Log.i(TAG,"rectangle size at ${Size((x2 - x1), (y2 - y1))}")
                    }

                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(1f)
        ) {

            Button(onClick = { takeImage() }) {
                Text("Take Picture")
            }
            Button(onClick = {
                //viewModel.isAcross.value = true
                setClueScanDirection(ClueDirection.ACROSS)
                scanClues()
            }) {
                Text("Across")
            }
            Button(onClick = {
                setClueScanDirection(ClueDirection.DOWN)
                scanClues()
            }) {
                Text("Down")
            }
        }

        Row(horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth(1f).padding(10.dp)
        ){
            LazyColumn(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(0.5f)
            ){
                items(uiState.value.acrossClues){ clue->
                    clueTextBox(clueData = clue,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary)
                }
            }
            LazyColumn(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(1f)
            ){
                items(uiState.value.downClues){ clue->
                    clueTextBox(clueData = clue,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary)
                }
            }

        }
    }
}
