package com.jhb.crosswordScan.ui.puzzleScanningScreens

//import com.jhb.learn_opencv.Puzzle
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.roundToIntRect
import androidx.lifecycle.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.jhb.crosswordScan.data.Puzzle
import com.jhb.crosswordScan.data.getAcrossCluesAsPairs
import com.jhb.crosswordScan.data.getDownCluesAsPairs
import com.jhb.crosswordScan.ui.common.ClueDirection
import com.jhb.crosswordScan.ui.common.ScanUiState
import com.jhb.crosswordScan.ui.puzzleScanningScreens.gridScanScreen.GridScanUiState
import com.jhb.crosswordScan.util.assembleClues
import com.jhb.crosswordScan.util.cropToCrossword
import com.jhb.crosswordScan.util.drawCrosswordContour
import com.jhb.crosswordScan.util.getCrosswordContour
import com.jhb.crosswordScan.util.makeBinaryCrosswordImg
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.opencv.android.Utils
import org.opencv.core.Core.ROTATE_90_CLOCKWISE
import org.opencv.core.Core.rotate
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

private const val TAG = "CrosswordScanViewModel"

class CrosswordScanViewModel : ViewModel(
) {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState : StateFlow<ScanUiState> = _uiState

    private val _uiGridState = MutableStateFlow(
        GridScanUiState(
            gridPicDebug = null,
            gridPicProcessed = null
        )
    )
    val uiGridState : StateFlow<GridScanUiState> = _uiGridState
    private var mirror = false

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val cluePicDebug = mutableStateOf<Bitmap?>(null)
    private val cluePicDebugCropped = mutableStateOf<Bitmap?>(null)

    //TODO make this a stateflow or something
    private val _puzzle = MutableStateFlow(Puzzle())
    val puzzle : StateFlow<Puzzle> = _puzzle

    private val _takeSnapShot = mutableStateOf(false)
    private val takeSnapShot : State<Boolean> = _takeSnapShot

    private lateinit var _viewFinderImg : Mat // we want this to be set by the camera input
    private lateinit var _viewFinderImgWithContour : Mat // we want this to be set by the camera input
    private lateinit var contours: List<MatOfPoint>
    private var cwContourIndex by Delegates.notNull<Int>()
    //private val crosswordDetector = CrosswordDetector()

    private val viewFinderImg: Mat
        get() = _viewFinderImg
    val viewFinderImgWithContour: Mat
        get() = _viewFinderImgWithContour


    fun processPreview(inputImg : Mat) {
        _viewFinderImg = inputImg
        _viewFinderImgWithContour = _viewFinderImg.clone()
        val contourInfo = getCrosswordContour(_viewFinderImg)
        contours = contourInfo.first
        cwContourIndex = contourInfo.second
        drawCrosswordContour(_viewFinderImgWithContour,contours,cwContourIndex)

        if (takeSnapShot.value && contours.isNotEmpty()) {
            Log.i(TAG,"Processing puzzle")
            Log.d(TAG,"Contours size ${contours.size}, Contours index $cwContourIndex")
            _takeSnapShot.value = false
            val imArea = Imgproc.contourArea(contours[cwContourIndex])
            Log.d(TAG,"Contour area $imArea")
            // if the area is bigger than 100x100 pixels, then set the preprocessed images
            if( imArea > 100*100 ) {
                setPreprocessed()

            }
            // GlobalScope.launch {  }

        }
    }


    private fun setPreprocessed() {
        val croppedCrossword = cropToCrossword(contours[cwContourIndex], viewFinderImg)
        var binaryCrossword : Mat? = null
        if (croppedCrossword != null) {
            binaryCrossword = makeBinaryCrosswordImg(croppedCrossword)
        }
        if (binaryCrossword != null) {
            val puzzle = assembleClues(binaryCrossword)

            clearScan()

            var gridBitmap =
                Bitmap.createBitmap(
                    binaryCrossword.rows(),
                    binaryCrossword.cols(),
                    Bitmap.Config.ARGB_8888,

                )

            // The coordinate system of openCV seems to be rotated by 90deg (or mirrored LR?).
            // The clue numbering algorithm handles this, but maybe work can be
            // done to make it so there aren't any confusing rotations required.
            val rotBinaryImage = Mat()
            rotate(binaryCrossword,rotBinaryImage,ROTATE_90_CLOCKWISE)
            Utils.matToBitmap(rotBinaryImage, gridBitmap)
            gridBitmap = Bitmap.createScaledBitmap(gridBitmap, 500, 500, false)

            _uiGridState.update {
                it.copy(
                    gridPicProcessed = gridBitmap
                )
            }

            _puzzle.update {
                puzzle
                //assembleClues(binaryCrossword)
            }
//            _puzzle.value =  assembleClues(binaryCrossword)

            _uiState.update {
                it.copy(
                    downClues = getDownCluesAsPairs(puzzle),
                    acrossClues = getAcrossCluesAsPairs(puzzle)
                )
            }
        }
    }

    fun clearScan(){
        clearPuzzleData()
        Log.i(TAG,"Resetting sreens")

        _uiState.update {
            ScanUiState()
        }
        _uiGridState.update {
            GridScanUiState(
                gridPicDebug = null,
                gridPicProcessed = null
            )
        }
    }

    private fun clearPuzzleData(){
        Log.i(TAG,"Deleting Puzzle data")
        _puzzle.update {
            Puzzle()
        }
    }

    fun resetClueHighlightBox(point : Offset) : Offset {
        Log.i(TAG,"start: $point")
        val newPoints = mutableListOf(point)
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
        cluePicDebug.value?.let { originalImage->
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

            cluePicDebugCropped.value =
                Bitmap.createBitmap(
                    originalImage,
                    cropRect.left,
                    cropRect.top,
                    cropRect.width,
                    cropRect.height
                )

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

    private fun ocrClues()  {
        Log.i(TAG,"performing OCR on clues")
        val imageForProcessing = cluePicDebugCropped.value //croppedCluePic.value
        if(imageForProcessing != null) {
            val image = InputImage.fromBitmap(imageForProcessing, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = Regex("\n").replace(visionText.text," ")
                    //split around things that look like (4) or (4,3] etc.
                    val regex = Regex("(?<=[(\\[][^A-Za-z]{0,27}[)\\]])")
                    val matchResult = regex.split(text)
                    val newClues = mutableListOf<Pair<String,String>>()

                    when(uiState.value.clueScanDirection){
                        ClueDirection.ACROSS -> {
                            matchResult.forEach {
                                val clueText = extractClueText(it)
                                val clueNum = extractClueNumber(it)
                                if (clueText != null) {
                                    val cluePair = Pair(clueNum + "a",clueText)
                                    newClues.add(cluePair)
                                    puzzle.value.updateClueTxt(clueNum + "a",clueText)
                                }
                            }
                            _uiState.update {
                                it.copy(
                                    acrossClues = getAcrossCluesAsPairs(puzzle.value)
                                )
                            }

                        }
                        ClueDirection.DOWN -> {
                            matchResult.forEach {
                                val clueText = extractClueText(it)
                                val clueNum = extractClueNumber(it)
                                if (clueText != null) {
                                    val cluePair = Pair(clueNum + "d",clueText)
                                    newClues.add(cluePair)
                                    puzzle.value.updateClueTxt(clueNum + "d",clueText)
                                }
                            }

                            _uiState.update {
                                it.copy(
                                    downClues = getDownCluesAsPairs(puzzle.value)
                                )
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.message?.let { Log.e(TAG, it) }
                }
        }
    }

    private fun extractClueNumber(unprocessed : String):  String{
        val regex = Regex("\\d+")
        val processed = regex.find(unprocessed)
        return processed?.value ?: "No clue found"
    }

    private fun extractClueText(unprocessed : String):  String? {
        val regex = Regex("(?<=\\d)(?!\\d).+")
        return regex.find(unprocessed)?.value
    }

    fun openCVCameraSize(width: Int, height: Int) {

        _uiGridState.update {
            it.copy(
                previewWidth = width,
                previewHeight = height
            )
        }
    }

    fun setSnapShotTrue(){
        Log.i(TAG,"Setting snapshot to true")
        _takeSnapShot.value = true
    }

    fun replaceClueText(old : Pair<String, String>, new : Pair<String, String>){
        puzzle.value.updateClueTxt(old.first,new.second)

        _uiState.update {
            it.copy(
                acrossClues = getAcrossCluesAsPairs(puzzle.value),
                downClues = getDownCluesAsPairs(puzzle.value)
            )
        }
    }

}

class CrosswordScanViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.i("CrosswordScanViewModelFactory","Creating")
        if (modelClass.isAssignableFrom(CrosswordScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CrosswordScanViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}