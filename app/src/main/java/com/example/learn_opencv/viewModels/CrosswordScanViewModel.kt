package com.example.learn_opencv.viewModels

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.*
import com.example.learn_opencv.CrosswordDetector
//import com.example.learn_opencv.Puzzle
import com.example.learn_opencv.data.Puzzle
import com.example.learn_opencv.data.PuzzleData
import com.example.learn_opencv.data.PuzzleRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class CrosswordScanViewModel(private val repository: PuzzleRepository): ViewModel(
) {
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
        gridImgResize.postValue(
            Bitmap.createBitmap(gridBitmap, 0, 0, gridBitmap.width, gridBitmap.height, matrix, true)
        )
        _puzzle.value = crosswordDetector.assembleClues()
        //_puzzle.value.setGridSize(gridBitmap)

    }

    fun ocrClues()  {
        val imageForProcessing = croppedCluePic.value
        if(imageForProcessing != null) {
            val image = InputImage.fromBitmap(imageForProcessing, 0)
            val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = Regex("\n").replace(visionText.text," ")
                    //split around things that look like (4) or (4,3] etc.
                    val regex = Regex("(?<=[\\(\\[][^A-Za-z]{0,27}[\\)\\]])")
                    val matchResult = regex.split(text)

                    if(isAcross.value) {
                        acrossClues.clear()
                        matchResult.forEach {
                            val cluetxt = extractClueText(it)
                            val clueNum = extractClueNumber(it)
                            if (cluetxt != null && clueNum != null) {
                                val cluePair = Pair(clueNum + "a",cluetxt)
                                acrossClues.add(cluePair)
                                puzzle.value.updateClueTxt(clueNum + "a",cluetxt)
                            }
                        }
                    }
                    else{
                        downClues.clear()
                        matchResult.forEach {
                            val cluetxt = extractClueText(it)
                            val clueNum = extractClueNumber(it)
                            if (cluetxt != null && clueNum != null) {
                                val cluePair = Pair(clueNum + "d",cluetxt)
                                downClues.add(cluePair)
                                puzzle.value.updateClueTxt(clueNum + "d",cluetxt)
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