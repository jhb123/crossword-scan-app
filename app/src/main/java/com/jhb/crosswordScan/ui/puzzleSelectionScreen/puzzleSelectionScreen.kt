package com.jhb.crosswordScan.ui.puzzleSelectionScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.SpaceEvenly
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhb.crosswordScan.PuzzleApplication
import com.jhb.crosswordScan.R
import com.jhb.crosswordScan.data.PuzzleData
import com.jhb.crosswordScan.data.deletePuzzleFiles
import com.jhb.crosswordScan.util.TimeStampFormatter
import com.jhb.crosswordScan.viewModels.PuzzleSelectViewModel
import com.jhb.crosswordScan.viewModels.PuzzleSelectViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private val TAG = "puzzleSelectionScreen"

@Composable
fun puzzleSelectionScreen(navigateToPuzzle: (Int, Boolean)->Unit){

    val application = (LocalContext.current.applicationContext as PuzzleApplication)
    val puzzleSelectViewModel : PuzzleSelectViewModel = viewModel(
        factory = PuzzleSelectViewModelFactory(application.repository)
    )

    val uiState by puzzleSelectViewModel.uiState.collectAsState()
    val repository = (LocalContext.current.applicationContext as PuzzleApplication).repository
    val composableScope = rememberCoroutineScope()

    puzzleSelectionComposable(
        uiState = uiState,
        navigateToPuzzle = navigateToPuzzle,
        uploadNewPuzzle = {
                puzzleSelectViewModel.uploadNewPuzzle(it,repository)},
        deletePuzzle = {
            composableScope.launch(Dispatchers.IO) {
                deletePuzzleFiles(it)
                repository.deletePuzzle(it)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun puzzleSelectionComposable(
    uiState : PuzzleSelectionUiState,
    navigateToPuzzle : (Int, Boolean) ->  Unit,
    uploadNewPuzzle: (PuzzleData) -> Unit,
    deletePuzzle: (PuzzleData) -> Unit
) {


    val puzzles = uiState.puzzles
    val clipboardManager = LocalClipboardManager.current
    // val composableScope = rememberCoroutineScope()
    val openDialog = remember { mutableStateOf(false) }
    val puzzleToDelete = remember { mutableStateOf<PuzzleData?>(null) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false }
        )
        {
            Surface(
                modifier = Modifier
                    //.wrapContentWidth()
                    .width(200.dp)
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            //composableScope.launch(Dispatchers.IO) {
                                puzzleToDelete.value?.let { deletePuzzle(it) }
                                openDialog.value = false
                            //}
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )

                    ) {
                        Text(text = stringResource(id = R.string.action_delete))
                    }
                    OutlinedButton(onClick = { openDialog.value = false }) {
                        Text(text = stringResource(id = R.string.action_cancellation))
                    }
                }
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(10.dp),
        modifier = Modifier
            .padding(0.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        uiState.errorText?.also {
            item{
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(1f)
                        .padding(5.dp)
                ){
                    Text(text=it, modifier=Modifier.padding(5.dp))
                }
            }
        }

        items(puzzles) { puzzleData ->
            lateinit var color: CardColors
            lateinit var contents: @Composable() (ColumnScope.() -> Unit)

            val offline = uiState.isOffline //&& puzzleData.serverId != null
            val onlinePuzzle = puzzleData.serverId != null

            if ( !(onlinePuzzle && offline) ) {
                if (onlinePuzzle) {
                    color = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    contents = RemoteCardContents(puzzleData, clipboardManager)
                } else {
                    color = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    contents = LocalCardContents(puzzleData, uploadNewPuzzle)
                }

                PuzzleCard(
                    puzzleData,
                    navigateToPuzzle,
                    onlinePuzzle,
                    openDialog,
                    color,
                    contents
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PuzzleCard(
    puzzleData: PuzzleData,
    navigateToPuzzle: (Int, Boolean) -> Unit,
    navigationRemote: Boolean,
    openDialog: MutableState<Boolean>,
    colors: CardColors,
    contents: @Composable() (ColumnScope.() -> Unit)
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = colors,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(1f)
            .padding(5.dp)
            .combinedClickable(
                onClick = { navigateToPuzzle(puzzleData.id, navigationRemote) }
            ),
//                onLongClick = {
//                    puzzleToDelete.value = puzzleData
//                    openDialog.value = true
//                },
        content = contents
    )
}

@Composable
private fun LocalCardContents(
    puzzleData: PuzzleData,
    uploadNewPuzzle: (PuzzleData) -> Unit
): @Composable() (ColumnScope.() -> Unit) =
    {
        //Box(Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize(1f)
        ) {
            Column(
                verticalArrangement = SpaceEvenly,
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(10.dp)
            ) {
                puzzleData.name?.let {
                    Text(
                        it,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }
                puzzleData.timeCreated?.let { it ->
                    val timeStampFormatter = TimeStampFormatter()
                    val timestamp = timeStampFormatter.friendlyTimeStamp(it)
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontStyle = MaterialTheme.typography.labelLarge.fontStyle,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize,
                                    fontWeight = MaterialTheme.typography.labelLarge.fontWeight,
                                )
                            ) {
                                append(stringResource(id = R.string.label_created))
                                append(stringResource(id = R.string.stringSeparator))

                            }
                            append(timestamp)
                        }
                    )
                }
            }

            IconButton(
                onClick = {uploadNewPuzzle(puzzleData)},
                modifier = Modifier.padding(10.dp)
            ) { Icon(
                    painterResource(
                        id = R.drawable.ic_baseline_file_upload_24),
                        contentDescription = stringResource(id = R.string.contentDesc_uploadPuzzle
                        )
                    )
                }
            }
    }




@Composable
private fun RemoteCardContents(
    puzzleData: PuzzleData,
    clipboardManager: ClipboardManager,
): @Composable() (ColumnScope.() -> Unit) =
    {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize(1f)
        ) {
            Column(
                verticalArrangement = SpaceEvenly,
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(10.dp)
            ) {
                puzzleData.name?.let {
                    Text(
                        it,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }
                puzzleData.timeCreated?.let { it ->
                    val timeStampFormatter = TimeStampFormatter()
                    val timestamp = timeStampFormatter.friendlyTimeStamp(it)
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontStyle = MaterialTheme.typography.labelLarge.fontStyle,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize,
                                    fontWeight = MaterialTheme.typography.labelLarge.fontWeight,
                                )
                            ) {
                                append(stringResource(id = R.string.label_created))
                                append(stringResource(id = R.string.stringSeparator))

                            }
                            append(timestamp)
                        }
                    )
                }
            }
        }
    }


