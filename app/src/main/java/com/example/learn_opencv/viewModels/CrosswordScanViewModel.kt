package com.example.learn_opencv.viewModels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.example.learn_opencv.CrosswordDetector
import com.example.learn_opencv.Puzzle
import com.example.learn_opencv.PuzzleData
import com.example.learn_opencv.PuzzleRepository
import com.google.mlkit.vision.text.Text
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

    private var TAG = "CrosswordScanViewModel"

    //val allPuzzles: LiveData<List<PuzzleData>> = repository.allPuzzles.asLiveData()

    private val _puzzle = mutableStateOf(Puzzle())
    val puzzle : State<Puzzle> = _puzzle

    private val _currentClue = mutableStateOf("")
    val currentClue : State<String> = _currentClue

    fun setActiveClue(newClue: String){
        _currentClue.value = newClue
    }

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

    //val clueText = mutableStateListOf<List<Text.TextBlock>>()
    //val clueText : LiveData<List<Text.TextBlock>> = _clueText

//    val gridImg: MutableLiveData<Bitmap> by lazy {
//        MutableLiveData<Bitmap>()
//    }
    fun insert() = viewModelScope.launch {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        val icon_uuid = UUID.randomUUID().toString()
        val fileContents = "Hello world!"
//        getApplication<Application>().applicationContext.openFileOutput(icon_uuid, Context.MODE_PRIVATE).use {
//            it.write(fileContents.toByteArray())
//       }

        val puzzleData = PuzzleData(currentDate, puzzle.value)
        repository.insert(puzzleData)
    }

    private val gridImgResize = MutableLiveData<Bitmap>()
    fun getGridImgResize() = gridImgResize

    private val gridImg = MutableLiveData<Bitmap>()
    fun getGridImg() = gridImg

    val cluePicDebug = MutableLiveData<Bitmap>()
    fun getcluePicDebug() = cluePicDebug

    val clueTextDebug = MutableLiveData<String>()

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
//        val rectToCrop = Rect(0, 0, 500, 500)
//        val cropped = inputWarp.submat(rectToCrop)
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
//        crosswordDetector.getGridWithClueMarks()
//        crosswordDetector.getAcrossClues()
//        crosswordDetector.getDownClues()
//        crosswordDetector.assembleClues()

        var gridBitmap =
            Bitmap.createBitmap(crosswordDetector.binaryCrosswordImg.cols(),
                crosswordDetector.binaryCrosswordImg.rows(), Bitmap.Config.ARGB_8888)


        Utils.matToBitmap(crosswordDetector.binaryCrosswordImg, gridBitmap);
        gridBitmap = Bitmap.createScaledBitmap(gridBitmap,500,500,false)
        gridImgResize.postValue(
            Bitmap.createBitmap(gridBitmap, 0, 0, gridBitmap.width, gridBitmap.height, matrix, true)
        )
        _puzzle.value = crosswordDetector.assembleClues()

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