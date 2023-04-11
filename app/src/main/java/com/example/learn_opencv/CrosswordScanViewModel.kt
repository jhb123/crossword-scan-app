package com.example.learn_opencv

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Rect
import kotlin.properties.Delegates

class CrosswordScanViewModel: ViewModel() {

    private var TAG = "CrosswordScanViewModel"
    var takeSnapshot = false
    //private lateinit var gridImg : Bitmap
    lateinit var viewFinderImg : Mat // we want this to be set by the camera input
    private lateinit var contours: List<MatOfPoint>
    private var cwContourIndex by Delegates.notNull<Int>()
    private val crosswordDetector = CrosswordDetector()

//    val gridImg: MutableLiveData<Bitmap> by lazy {
//        MutableLiveData<Bitmap>()
//    }

    private val gridImg = MutableLiveData<Bitmap>()
    fun getGridImg() = gridImg

    fun processPreview() {
        val contourInfo = crosswordDetector.get_crossword_contour(viewFinderImg)
        contours = contourInfo.first
        cwContourIndex = contourInfo.second
        crosswordDetector.draw_crossword_contour(viewFinderImg,contours,cwContourIndex)

        if (takeSnapshot && contours.isNotEmpty()) {
            Log.d(TAG,"Contours size ${contours.size}, Contours index $cwContourIndex")
            takeSnapshot = false
            setPreprocessed()
            // GlobalScope.launch {  }

        }
    }

    fun setPreprocessed() {
        Log.d(TAG, "Setting snapshot preview image")
        val inputWarp = crosswordDetector.crop_to_crossword(contours[cwContourIndex], viewFinderImg)
        val rectToCrop = Rect(0, 0, 500, 500)
        val cropped = inputWarp.submat(rectToCrop)
        Log.d(TAG, "Making bitmap")
        val bitmap =
            Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(cropped, bitmap);
        val matrix = Matrix()
        matrix.postRotate(90f)
        gridImg.postValue(
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        )
        //Log.d(TAG, "Displaying bitmap")
        }

//        GlobalScope.launch {
//            processed_preview.updateBitmap(bitmap_rot)
//        }

//    val viewFinderImg : Mat
//        get() = _viewFinderImg
//
//    fun setViewFinderImg(img: Mat) {
//        _viewFinderImg =  img
//    }


}