package com.jhb.crosswordScan.ui.puzzleScanningScreens.clueScanScreen

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhb.crosswordScan.MainActivity
import com.jhb.crosswordScan.PuzzleApplication
import com.jhb.crosswordScan.ui.common.ClickableClueTextBox
import com.jhb.crosswordScan.ui.common.ClueDirection
import com.jhb.crosswordScan.ui.common.ScanUiState
import com.jhb.crosswordScan.viewModels.CrosswordScanViewModel
import com.jhb.crosswordScan.viewModels.CrosswordScanViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

private const val TAG = "clueScanComposable"

@Composable
fun clueScanScreen(
    viewModel: CrosswordScanViewModel = viewModel(
        factory = CrosswordScanViewModelFactory(
            (LocalContext.current.applicationContext as PuzzleApplication).repository
        ),
        viewModelStoreOwner = (LocalContext.current as ComponentActivity)
    )
){

    //we need this to get the intent to launch taking a photo
    //val mainActivity = (LocalContext.current as MainActivity)
    val photoLauncher = (LocalContext.current as MainActivity).photoLauncher

    photoLauncher.bitmap = viewModel.cluePicDebug

    val uiState by viewModel.uiState.collectAsState()
    val composableScope = rememberCoroutineScope()

    val bitmap = viewModel.cluePicDebug.value

    ClueScanComposable(
        uiState = uiState,
        cluePicDebug = bitmap,
        setClueScanDirection = { viewModel.setClueScanDirection(it) },
        takeImage = {
            composableScope.launch { photoLauncher.takeImage() }
        },
        onDragStart = { viewModel.resetClueHighlightBox(it) },
        onDrag = { viewModel.changeClueHighlightBox(it) },
        setCanvasSize = { viewModel.setCanvasSize(it) },
        scanClues = { viewModel.scanClues() },
        updateClueText = { oldClue, newClue -> viewModel.replaceClueText(oldClue, newClue)}
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClueScanComposable(
    uiState: ScanUiState,
    cluePicDebug : Bitmap?,
    setClueScanDirection : (ClueDirection) -> Unit,
    takeImage : () -> Unit,
    onDragStart : (Offset) -> Offset,
    onDrag : (PointerInputChange) -> PointerInputChange,
    setCanvasSize : (Size) -> Unit,
    scanClues : () -> Unit,
    updateClueText : ( Pair<String,String>,Pair<String,String> ) -> Unit
    //points: () -> List<Offset>
)
{

    //val cluePicDebug = uiState.cluePicDebug
    val canvasOffset = uiState.canvasOffset

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
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.large
                )
                .clip(MaterialTheme.shapes.large)
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

                    if(uiState.selectedPoints.isNotEmpty()){
                        val x1 = min(uiState.selectedPoints.first().x,uiState.selectedPoints.last().x)
                        val y1 = min(uiState.selectedPoints.first().y,uiState.selectedPoints.last().y)
                        val x2 = max(uiState.selectedPoints.first().x,uiState.selectedPoints.last().x)
                        val y2 = max(uiState.selectedPoints.toList().first().y,uiState.selectedPoints.last().y)

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

            FilledTonalButton(onClick = { takeImage() }) {
                Text(text="Snap",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(60.dp)
                )
            }
            FilledTonalButton(onClick = {
                //viewModel.isAcross.value = true
                setClueScanDirection(ClueDirection.ACROSS)
                scanClues()
            }) {
                Text("Across",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(60.dp)
                )
            }
            FilledTonalButton(
                onClick = {
                setClueScanDirection(ClueDirection.DOWN)
                scanClues() },
                //colors = //ButtonDefaults.elevatedButtonColors()
            ) {
                Text("Down",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(60.dp)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(10.dp)
        ){
            var openDialog by remember { mutableStateOf(false) }
            var oldClue by remember { mutableStateOf(Pair("","")) }
            var newClue by remember { mutableStateOf(Pair("","")) }


            if (openDialog) {
                AlertDialog(
                    onDismissRequest = { openDialog = false }
                )
                {
                    Surface(
                        modifier = Modifier
                            .wrapContentWidth()
                            .wrapContentHeight(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column() {

                            Text(
                                text = "Edit ${oldClue.first}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(20.dp)
                            )
                            OutlinedTextField(
                                value = newClue.second,
                                onValueChange = {newClue = Pair(newClue.first, it)},
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                minLines = 6,
                                maxLines = 6,
                            )

                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        //composableScope.launch(Dispatchers.IO) {
                                        updateClueText( oldClue , newClue )
                                        openDialog = false
                                        //}
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )

                                ) {
                                    Text(text = "Confirm")
                                }
                                OutlinedButton(onClick = { openDialog = false }) {
                                    Text(text = "Cancel")
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(0.5f)
            ){
                items(uiState.acrossClues){ clue->
                    //clueTextBox(clueData = clue)
                    ClickableClueTextBox(
                        clueData = clue,
                        onClick =  {
                            openDialog = true
                            oldClue = clue
                            newClue = clue

                        }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(1f)
            ){
                items(uiState.downClues){ clue->
                    ClickableClueTextBox(
                        clueData = clue,
                        onClick =  {
                            openDialog = true
                            oldClue = clue
                            newClue = clue

                        }
                    )
                }
            }

        }
    }
}

