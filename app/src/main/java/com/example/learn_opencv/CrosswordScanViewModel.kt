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
    private lateinit var _viewFinderImg : Mat // we want this to be set by the camera input
    private lateinit var _viewFinderImgWithContour : Mat // we want this to be set by the camera input
    private lateinit var contours: List<MatOfPoint>
    private var cwContourIndex by Delegates.notNull<Int>()
    private val crosswordDetector = CrosswordDetector()

    val viewFinderImg: Mat
        get() = _viewFinderImg
    val viewFinderImgWithContour: Mat
        get() = _viewFinderImgWithContour

//    val gridImg: MutableLiveData<Bitmap> by lazy {
//        MutableLiveData<Bitmap>()
//    }

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
            setPreprocessed()

            // GlobalScope.launch {  }

        }
    }

    fun setPreprocessed() {

        Log.d(TAG, "Setting snapshot preview image")
        crosswordDetector.crop_to_crossword(contours[cwContourIndex], viewFinderImg)
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

        val gridBitmap =
            Bitmap.createBitmap(crosswordDetector.binaryCrosswordImg.cols(),
                crosswordDetector.binaryCrosswordImg.rows(), Bitmap.Config.ARGB_8888)


        Utils.matToBitmap(crosswordDetector.binaryCrosswordImg, gridBitmap);

        gridImgResize.postValue(
            Bitmap.createBitmap(gridBitmap, 0, 0, gridBitmap.width, gridBitmap.height, matrix, true)
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