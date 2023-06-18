package com.jhb.crosswordScan.ui.puzzleSelectionScreen

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhb.crosswordScan.PuzzleApplication
import com.jhb.crosswordScan.R
import com.jhb.crosswordScan.data.PuzzleData
import com.jhb.crosswordScan.ui.common.Spinner
import com.jhb.crosswordScan.util.TimeStampFormatter
import com.jhb.crosswordScan.viewModels.PuzzleSelectViewModel
import com.jhb.crosswordScan.viewModels.PuzzleSelectViewModelFactory
import java.io.File
import java.time.format.DateTimeFormatter


private val TAG = "puzzleSelectionScreen"

@Composable
fun puzzleSelectionScreen(navigateToPuzzle: (String)->Unit){

    val application = (LocalContext.current.applicationContext as PuzzleApplication)
    val puzzleSelectViewModel : PuzzleSelectViewModel = viewModel(
        factory = PuzzleSelectViewModelFactory(application.repository)
    )

    val uiState by puzzleSelectViewModel.uiState.collectAsState()
    val filesDir = LocalContext.current.applicationContext.filesDir
    val repository = (LocalContext.current.applicationContext as PuzzleApplication).repository
    //val composableScope = rememberCoroutineScope()

    puzzleSelectionComposable(
        uiState = uiState,
        navigateToPuzzle = navigateToPuzzle,
        searchPuzzle = {puzzleSelectViewModel.getPuzzle(filesDir)},
        setSearchText = {puzzleSelectViewModel.updateSearch(it)},
        uploadNewPuzzle = {
                puzzleSelectViewModel.uploadNewPuzzle(it,repository)}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun puzzleSelectionComposable(
    uiState : PuzzleSelectionUiState,
    navigateToPuzzle : (String) ->  Unit,
    searchPuzzle : () -> Unit,
    setSearchText : (String) -> Unit,
    uploadNewPuzzle: (PuzzleData) -> Unit
){

    val puzzles = uiState.puzzles
    val clipboardManager = LocalClipboardManager.current
    val formatter = DateTimeFormatter.ofPattern("MMM dd hh:mm:ss");


    LazyColumn(contentPadding = PaddingValues(10.dp),
        modifier = Modifier
            .padding(0.dp)
            .background(MaterialTheme.colorScheme.background)
    ){
        item{
            OutlinedTextField(
                value = uiState.searchGuid,
                modifier = Modifier
                    .padding(10.dp)
                    .fillParentMaxWidth(1f),
                onValueChange = { setSearchText(it) },
                label = { Text("Puzzle ID") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, autoCorrect = false),
                keyboardActions = KeyboardActions(
                    onSearch = {searchPuzzle()}
                ),
                leadingIcon = {
                    IconButton(onClick = { searchPuzzle() }) {
                        Icon(painterResource(id = R.drawable.ic_baseline_search_24),
                            contentDescription = "search")
                    }
                },
                trailingIcon = {
                    if(uiState.isLoading){
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
        if(uiState.imageTest != null){
            item {
                Image(
                    painter = BitmapPainter(uiState.imageTest!!.asImageBitmap()),
                    contentDescription = "imagetest",
                    modifier = Modifier
                        .width(100.dp)
                        .padding(10.dp)
                )
            }
        }
        items(puzzles){ puzzleData->
            Card(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                //shape = RoundedCornerShape(10.dp),
                onClick = {
                    Log.i(TAG, "Card clicked")
                    navigateToPuzzle(puzzleData.id)
                    //navigateToPuzzle(puzzles.indexOf(puzzleData))
                          },
                modifier = Modifier
                    .height(120.dp)
                    .fillParentMaxWidth(1f)
                    .padding(5.dp)
            ) {
                //Box(Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize(1f)
                ) {
                    Column(modifier = Modifier
                        .width(100.dp)
                        ) {
                        val file = File(puzzleData.puzzleIcon)
                        val bmOptions = BitmapFactory.Options()
                        val bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions)
                        if (bitmap != null) {
                            Image(
                                painter = BitmapPainter(bitmap.asImageBitmap()),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.height(100.dp).padding(10.dp)
                    ) {
                        val timeStampFormatter = TimeStampFormatter()
                        val timeCreated = timeStampFormatter.friendlyTimeStamp(puzzleData.timeCreated)
                        val timeModified = timeStampFormatter.friendlyTimeStamp(puzzleData.lastModified)

                        Text(
                            buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        fontStyle = MaterialTheme.typography.labelLarge.fontStyle,
                                        fontSize = MaterialTheme.typography.labelLarge.fontSize,
                                        fontWeight = MaterialTheme.typography.labelLarge.fontWeight,
                                    )
                                ){
                                    append("Created: ")
                                }
                                append(timeCreated)
                            }
                        )

                        Text(
                            buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        fontStyle = MaterialTheme.typography.labelLarge.fontStyle,
                                        fontSize = MaterialTheme.typography.labelLarge.fontSize,
                                        fontWeight = MaterialTheme.typography.labelLarge.fontWeight,
                                    )
                                ){
                                    append("Modified: ")
                                }
                                append(timeModified)
                            }
                        )
                    }

                    IconButton(
                        onClick = {
                            if(puzzleData.isShared){
                                clipboardManager.setText(AnnotatedString(text = puzzleData.id))
                            }
                            else {
                                uploadNewPuzzle(puzzleData)
                            }
                                  },
                        modifier = Modifier.padding(10.dp)
                    ) {
                        if(puzzleData.isShared){
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_content_copy_24),
                                contentDescription = "copy GUID"
                            )
                        }
                        else {
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_file_upload_24),
                                contentDescription = "upload Puzzle"
                            )
                        }
                    }
                }
            }
        }
    }
}