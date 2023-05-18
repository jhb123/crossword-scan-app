package com.example.learn_opencv.viewModels

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.roundToIntRect
import androidx.lifecycle.*
import com.example.learn_opencv.CrosswordDetector
import com.example.learn_opencv.data.Clue
//import com.example.learn_opencv.Puzzle
import com.example.learn_opencv.data.Puzzle
import com.example.learn_opencv.data.PuzzleData
import com.example.learn_opencv.data.PuzzleRepository
import com.example.learn_opencv.ui.common.ClueDirection
import com.example.learn_opencv.ui.common.ScanUiState
import com.example.learn_opencv.ui.gridScanScreen.GridScanUiState
import com.example.learn_opencv.ui.solveScreen.PuzzleUiState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

class CrosswordScanViewModel(private val repository: PuzzleRepository): ViewModel(
) {

    private val _uiState = MutableStateFlow(
        ScanUiState(
            canvasOffset = Offset(0f,0f),
            canvasSize = Size(0f,0f),
            cluePicDebug = null,
            croppedCluePic = null,
            clueScanDirection = ClueDirection.ACROSS,
            isScrollingCanvas = false,
            selectedPoints = mutableListOf<Offset>(),
            gridPic = null,
            acrossClues = listOf<Pair<String,String>>(),
            downClues = listOf<Pair<String,String>>()
        )
    )

    val uiState : StateFlow<ScanUiState> = _uiState

    private val _uiGridState = MutableStateFlow(
        GridScanUiState(
            gridPicDebug = null,
            gridPicProcessed = null
        )
    )
    val uiGridState : StateFlow<GridScanUiState> = _uiGridState


    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    val croppedCluePic = MutableLiveData<Bitmap>()
    val acrossClues =  SnapshotStateList<Pair<String, String>>()
    val downClues =  SnapshotStateList<Pair<String, String>>()

    val isAcross = mutableStateOf(true)

    private val _cluePicDebug = MutableLiveData<Bitmap>()
    val cluePicDebug : LiveData<Bitmap> = _cluePicDebug
    fun updateCluePicDebug(bitmap: Bitmap){
        _cluePicDebug.postValue(bitmap)
    }

    private var TAG = "CrosswordScanViewModel"

    private val _puzzle = mutableStateOf(Puzzle())
    val puzzle : State<Puzzle> = _puzzle

    private val _currentClueName = mutableStateOf("")
    val currentClueName : State<String> = _currentClueName

    var takeSnapshot = false

    //private lateinit var gridImg : Bitmap
    private lateinit var _viewFinderImg : Mat // we want this to be set by the camera input
    private lateinit var _viewFinderImgWithContour : Mat // we want this to be set by the camera input
    private lateinit var contours: List<MatOfPoint>
    private var cwContourIndex by Delegates.notNull<Int>()
    private val crosswordDetector = CrosswordDetector()

    val viewFinderImg: Mat
        get() = _viewFinderImg
    val viewFinderImgWithContour: Mat
        get() = _viewFinderImgWithContour


    fun insert() = viewModelScope.launch {
        Log.i(TAG,"inserting new puzzle")
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        val icon_uuid = UUID.randomUUID().toString()
        val fileContents = "Hello world!"

        val puzzleData = PuzzleData(currentDate, puzzle.value)
        repository.insert(puzzleData)
    }

    private val gridImgResize = MutableLiveData<Bitmap>()
    fun getGridImgResize() = gridImgResize

    private val gridImg = MutableLiveData<Bitmap>()
    fun getGridImg() = gridImg

    fun processPreview(inputImg : Mat) {
        _viewFinderImg = inputImg
        _viewFinderImgWithContour = _viewFinderImg.clone()
        val contourInfo = crosswordDetector.get_crossword_contour(_viewFinderImg)
        contours = contourInfo.first
        cwContourIndex = contourInfo.second
        crosswordDetector.draw_crossword_contour(_viewFinderImgWithContour,contours,cwContourIndex)

        if (takeSnapshot && contours.isNotEmpty()) {
            Log.d(TAG,"Contours size ${contours.size}, Contours index $cwContourIndex")
            takeSnapshot = false
            val imArea = Imgproc.contourArea(contours[cwContourIndex])
            Log.d(TAG,"Contour area $imArea")
            // if the area is bigger than 100x100 pixels, then set the preprocessed images
            if( imArea > 100*100 ) {
                setPreprocessed()
            }
            // GlobalScope.launch {  }

        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun setPreprocessed() {

        Log.d(TAG, "Setting snapshot preview image")
        crosswordDetector.cropToCrossword(contours[cwContourIndex], viewFinderImg)
        Log.d(TAG, "Making bitmap")
        val bitmap =
            Bitmap.createBitmap(crosswordDetector.croppedToCrosswordImg.cols(),
                crosswordDetector.croppedToCrosswordImg.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(crosswordDetector.croppedToCrosswordImg, bitmap);
        val matrix = Matrix()
        matrix.postRotate(90f)
        gridImg.postValue(
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        )

        crosswordDetector.makeBinaryCrosswordImg()

        var gridBitmap =
            Bitmap.createBitmap(crosswordDetector.binaryCrosswordImg.cols(),
                crosswordDetector.binaryCrosswordImg.rows(), Bitmap.Config.ARGB_8888)


        Utils.matToBitmap(crosswordDetector.binaryCrosswordImg, gridBitmap);
        gridBitmap = Bitmap.createScaledBitmap(gridBitmap,500,500,false)
//        gridImgResize.postValue(
//            Bitmap.createBitmap(gridBitmap, 0, 0, gridBitmap.width, gridBitmap.height, matrix, true)
//        )
        _uiGridState.update { it->
            it.copy(
                gridPicProcessed = gridBitmap
            )
        }

        _puzzle.value = crosswordDetector.assembleClues()
        //_puzzle.value.setGridSize(gridBitmap)

    }

    fun resetClueHighlightBox(point : Offset) : Offset {
        Log.i(TAG,"start: ${point}")
        val newPoints = mutableListOf(point)
//        _uiState.value.selectedPoints.clear()
//        _uiState.value.selectedPoints.add(point)
        _uiState.update {
            it.copy(
                selectedPoints = newPoints
            )
        }
        return point
    }

    fun changeClueHighlightBox(change : PointerInputChange) : PointerInputChange {
        Log.i(TAG,"change: ${change.position}")

        val newPoints = mutableListOf<Offset>()//_uiState.value.selectedPoints.add(change.position)
        newPoints.addAll(_uiState.value.selectedPoints)
        newPoints.add(change.position)
        _uiState.update {
            it.copy(
                selectedPoints = newPoints
            )
        }
        return change
    }

    fun scanClues() {
        uiState.value.cluePicDebug?.let { originalImage->
            var x1 = 0f
            var x2 = 0f//originalImage.width.toFloat()
            var y1 = 0f
            var y2 = 0f//originalImage.height.toFloat()

            val widthFactor = originalImage.width/uiState.value.canvasSize.width
            val heightFactor = originalImage.height/uiState.value.canvasSize.height
            Log.i(TAG,"widthFactor $widthFactor, heightFactor $heightFactor")

            val points = uiState.value.selectedPoints

            if (points.size > 1){
                // this is overly complex because I thought I'd be able to zoom as well.
                val leftDistance = (min(points.first().x,points.last().x) - uiState.value.canvasOffset.x)*widthFactor
                val rightDistance = (originalImage.width.toFloat() -
                        (max(points.first().x, points.last().x) - uiState.value.canvasOffset.x) * widthFactor)

                val topDistance = (min(points.first().y,points.last().y) - uiState.value.canvasOffset.y)*heightFactor
                val bottomDistance = (originalImage.height.toFloat() -
                        (max(points.first().y, points.last().y) - uiState.value.canvasOffset.y) * heightFactor)

                x1 = leftDistance
                x2 = rightDistance
                y1 = topDistance
                y2 = bottomDistance

            }
            if(x1 < 0){
                x1 = 0f
            }
            if(y1 < 0){
                y1 = 0f
            }
            if(x2 < 0){
                x2 = 0f
            }
            if(y2 < 0){
                y2 = 0f
            }

            Log.i(TAG, "rectangle drawn using x1: $x1, x2: $x2, y1: $y1, y2: $y2")
            Log.i(TAG,"rectangle offset at ${Offset(x1, y1)}")
            Log.i(TAG,"rectangle size at ${Size(originalImage.width.toFloat() - x1 - x2,
                    originalImage.height.toFloat() - y1 - y2)}")


            val cropRect = Rect(
                Offset(x1, y1),
                Size(originalImage.width.toFloat() - x1 - x2,
                    originalImage.height.toFloat() - y1 - y2)
            ).roundToIntRect()


            _uiState.update {
                it.copy(
                    croppedCluePic = Bitmap.createBitmap(
                        originalImage,
                        cropRect.left,
                        cropRect.top,
                        cropRect.width,
                        cropRect.height)
                )
            }

            ocrClues()
        }
    }

    fun setCanvasSize(size : Size){
        _uiState.update {
            it.copy(
                canvasSize = size
            )
        }
    }

    fun setClueScanDirection(direction : ClueDirection){
        _uiState.update {
            it.copy(
                clueScanDirection = direction
            )
        }

    }

    fun setCluePicDebug(image : Bitmap?){
        _uiState.update {
            it.copy(
                cluePicDebug = image
            )
        }

    }

    fun ocrClues()  {

        val imageForProcessing = uiState.value.croppedCluePic //croppedCluePic.value
        if(imageForProcessing != null) {
            val image = InputImage.fromBitmap(imageForProcessing, 0)
            val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = Regex("\n").replace(visionText.text," ")
                    //split around things that look like (4) or (4,3] etc.
                    val regex = Regex("(?<=[\\(\\[][^A-Za-z]{0,27}[\\)\\]])")
                    val matchResult = regex.split(text)
                    val newClues = mutableListOf<Pair<String,String>>()

                    when(uiState.value.clueScanDirection){
                        ClueDirection.ACROSS -> {
                            matchResult.forEach {
                                val cluetxt = extractClueText(it)
                                val clueNum = extractClueNumber(it)
                                if (cluetxt != null && clueNum != null) {
                                    val cluePair = Pair(clueNum + "a",cluetxt)
                                    newClues.add(cluePair)
                                    puzzle.value.updateClueTxt(clueNum + "a",cluetxt)
                                }
                            }
                            _uiState.update {
                                it.copy(
                                    acrossClues = newClues
                                )
                            }

                        }
                        ClueDirection.DOWN -> {
                            matchResult.forEach {
                                val cluetxt = extractClueText(it)
                                val clueNum = extractClueNumber(it)
                                if (cluetxt != null && clueNum != null) {
                                    val cluePair = Pair(clueNum + "d",cluetxt)
                                    newClues.add(cluePair)
                                    puzzle.value.updateClueTxt(clueNum + "d",cluetxt)
                                }
                            }
                            _uiState.update {
                                it.copy(
                                    downClues = newClues
                                )
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    //hmmm
                }
        }
    }

    fun extractClueNumber(unprocessed : String):  String{
        val regex = Regex("\\d+")
        val processed = regex.find(unprocessed)
        if (processed != null) {
            return processed.value
        }
        else{
            return "No clue found"
        }
    }

    fun extractClueText(unprocessed : String):  String?{
        val regex = Regex("(?<=\\d)(?!\\d).+")
        val processed = regex.find(unprocessed)
        if (processed != null) {
            return processed.value
        }
        else{
            return null
        }
    }

    fun openCVCameraSize(width: Int, height: Int) {

        _uiGridState.update {
            it.copy(
                previewWidth = width,
                previewHeight = height
            )
        }
    }

}

class CrosswordScanViewModelFactory(private val repository: PuzzleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.i("CrosswordScanViewModelFactory","Creating")
        if (modelClass.isAssignableFrom(CrosswordScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CrosswordScanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}