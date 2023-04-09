package com.example.learn_opencv

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.getRotation
import com.example.learn_opencv.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import org.opencv.android.*
import org.opencv.android.CameraBridgeViewBase.*
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.math.BigInteger

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }




//class MainActivity : CameraActivity(), CvCameraViewListener2 {
//    private val TAG = "openCV test app"
//
//    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
//    lateinit var scan_button : Button
//    lateinit var processed_preview : ImageView
//
//    private val crosswordDetector = CrosswordDetector()
//
//    val displayMetrics = DisplayMetrics()
//
//    var take_snap_shot = false
//
//
//    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
//        override fun onManagerConnected(status: Int) {
//            when (status) {
//                SUCCESS -> {
//                    Log.i(TAG, "OpenCV loaded successfully")
//                    mOpenCvCameraView.enableView()
//                }
//                else -> {
//                    super.onManagerConnected(status)
//                }
//            }
//        }
//    }
//
    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        Log.i(TAG,"Calling onCreate")
//
//        // set up open cv
//        if(OpenCVLoader.initDebug()){
//            Log.i(TAG,"OpenCV loaded!")
//        }
//
//        // check permissions
//        if (allPermissionsGranted()) {
//            //do something
//        } else {
//            ActivityCompat.requestPermissions(
//                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
//            )
//        }
//
//        // set up camera preview
//        Log.i(TAG,"Making Camera View")
//
//        val height = displayMetrics.heightPixels
//        val width = displayMetrics.widthPixels
//        Log.i(TAG,"setting height as $height and width as $width")
//
//        mOpenCvCameraView =
//            findViewById<View>(R.id.camera) as CameraBridgeViewBase
//        mOpenCvCameraView.setUserRotation(90)
//        mOpenCvCameraView.setCameraIndex(CAMERA_ID_BACK)
//        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
//        mOpenCvCameraView.setCvCameraViewListener(this)
//
//        scan_button = findViewById<Button>(R.id.scan_button)
//        scan_button.setOnClickListener{ set_snap_to_true() }
//
//        processed_preview = findViewById(R.id.processed_preview)
//    }
//
//
//
//    public override fun onPause() {
//        super.onPause()
//        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
//    }
//
//    public override fun onResume() {
//        super.onResume()
//        if (!OpenCVLoader.initDebug()) {
//            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
//        } else {
//            Log.d(TAG, "OpenCV library found inside package. Using it!")
//            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
//        }
//    }
//
//    override fun getCameraViewList(): List<CameraBridgeViewBase?>? {
//        return listOf<CameraBridgeViewBase>(mOpenCvCameraView)
//    }
//
//    public override fun onDestroy() {
//        super.onDestroy()
//        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
//    }
//
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
//
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                //startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
//
//    override fun onCameraViewStarted(width: Int, height: Int) {
//
//    }
//
//    override fun onCameraViewStopped() {
//
//    }
//
//    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
//
//        val mRgba = inputFrame!!.rgba()
//        val (contours,cw_contour) = crosswordDetector.get_crossword_contour(mRgba)
//        crosswordDetector.draw_crossword_contour(mRgba,contours,cw_contour)
//        // crosswordDetector.process(mRgba)
//        if (take_snap_shot && contours.isNotEmpty()) {
//            Log.d(TAG,"Contours size ${contours.size}, Contours index $cw_contour")
//            take_snap_shot = false
//            set_preprocessed(contours[cw_contour],mRgba)
//
//
//            // GlobalScope.launch {  }
//            //processed_preview.setImageBitmap(bitmap)
//
//        }
//        return mRgba
//
//    }
//
//    fun set_preprocessed(contour: MatOfPoint , image: Mat){
//        Log.d(TAG, "Setting snapshot preview image")
//        val input_warp = crosswordDetector.crop_to_crossword(contour, image)
//        val rect_to_crop = Rect(0, 0, 500, 500)
//        val cropped = input_warp.submat(rect_to_crop)
//        Log.d(TAG, "Making bitmap")
//        val bitmap =
//            Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(cropped, bitmap);
//        val matrix = Matrix()
//        matrix.postRotate(90f)
//        val bitmap_rot =
//            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//        Log.d(TAG, "Displaying bitmap")
//
//        GlobalScope.launch {
//            processed_preview.updateBitmap(bitmap_rot)
//        }
//
//    }
//
//    suspend fun ImageView.updateBitmap(bitmap: Bitmap) = withContext(Dispatchers.Main) {
//        setImageBitmap(bitmap)
//    }
//
//    fun set_snap_to_true(){
//        take_snap_shot = true
//    }
}