package com.jhb.crosswordScan.ui.puzzleSelectionScreen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.SpaceEvenly
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhb.crosswordScan.PuzzleApplication
import com.jhb.crosswordScan.R
import com.jhb.crosswordScan.data.PuzzleData
import com.jhb.crosswordScan.data.deletePuzzleFiles
import com.jhb.crosswordScan.ui.common.Spinner
import com.jhb.crosswordScan.util.TimeStampFormatter
import com.jhb.crosswordScan.viewModels.PuzzleSelectViewModel
import com.jhb.crosswordScan.viewModels.PuzzleSelectViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private val TAG = "puzzleSelectionScreen"

@Composable
fun puzzleSelectionScreen(navigateToPuzzle: (Int)->Unit){

    val application = (LocalContext.current.applicationContext as PuzzleApplication)
    val puzzleSelectViewModel : PuzzleSelectViewModel = viewModel(
        factory = PuzzleSelectViewModelFactory(application.repository)
    )

    val uiState by puzzleSelectViewModel.uiState.collectAsState()
    val filesDir = LocalContext.current.applicationContext.filesDir
    val repository = (LocalContext.current.applicationContext as PuzzleApplication).repository
    val composableScope = rememberCoroutineScope()

    puzzleSelectionComposable(
        uiState = uiState,
        navigateToPuzzle = navigateToPuzzle,
        searchPuzzle = {puzzleSelectViewModel.getPuzzle(filesDir)},
        setSearchText = {puzzleSelectViewModel.updateSearch(it)},
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
    navigateToPuzzle : (Int) ->  Unit,
    searchPuzzle : () -> Unit,
    setSearchText : (String) -> Unit,
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
        item {
            OutlinedTextField(
                value = uiState.searchGuid,
                modifier = Modifier
                    .padding(10.dp)
                    .fillParentMaxWidth(1f),
                onValueChange = { setSearchText(it) },
                label = { Text(stringResource(id = R.string.label_puzzleSearch)) },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    autoCorrect = false
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { searchPuzzle() }
                ),
                leadingIcon = {
                    IconButton(onClick = { searchPuzzle() }) {
                        Icon(
                            painterResource(id = R.drawable.ic_baseline_search_24),
                            contentDescription = stringResource(id = R.string.contentDesc_search)
                        )
                    }
                },
                trailingIcon = {
                    if (uiState.isLoading) {
                        Spinner()
                    }
                },
                supportingText = {
                    uiState.errorText?.let { Text(it) }
                },
                isError = uiState.errorText != null,
                singleLine = true
            )
        }
        items(puzzles) { puzzleData ->

            Card(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier
                    .height(120.dp)
                    .fillParentMaxWidth(1f)
                    .padding(5.dp)
                    .combinedClickable(
                        onClick = {
                            Log.i(TAG, "Card clicked")
                            navigateToPuzzle(puzzleData.id)
                            //navigateToPuzzle(puzzles.indexOf(puzzleData))
                        },
                        onLongClick = {
                            Log.i(TAG, "detected long click")
                            puzzleToDelete.value = puzzleData
                            openDialog.value = true
                        }
                    ),


                ) {
                //Box(Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize(1f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .height(100.dp)
                            .padding(10.dp)
                    ) {
                        val timeStampFormatter = TimeStampFormatter()
                        val timeCreated =
                            timeStampFormatter.friendlyTimeStamp(puzzleData.timeCreated)

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
                                append(timeCreated)
                            }
                        )
                    }

                    IconButton(
                        onClick = {
                            if (puzzleData.serverId != null) {
                                clipboardManager.setText(AnnotatedString(text = "${puzzleData.serverId}"))
                            } else {
                                uploadNewPuzzle(puzzleData)
                            }
                        },
                        modifier = Modifier.padding(10.dp)
                    ) {
                        if (puzzleData.serverId != null) {
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_content_copy_24),
                                contentDescription = stringResource(id = R.string.contentDesc_copyGuid)
                            )
                        } else {
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_file_upload_24),
                                contentDescription = stringResource(id = R.string.contentDesc_uploadPuzzle)
                            )
                        }
                    }
                }
            }
        }
    }
}

