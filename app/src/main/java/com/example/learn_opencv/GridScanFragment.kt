package com.example.learn_opencv

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.learn_opencv.databinding.FragmentGridScanBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.*
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Rect

/**
 * A simple [Fragment] subclass.
 * Use the [GridScanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GridScanFragment : Fragment() , CameraBridgeViewBase.CvCameraViewListener2 {

    private val TAG = "GridScanFragment"
    private var _binding: FragmentGridScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    private lateinit var scan_button : Button
    private lateinit var processed_preview : ImageView
    private val crosswordDetector = CrosswordDetector()


    var take_snap_shot = false

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        requestCameraPermissions()

    }

    private fun requestCameraPermissions() {
        // Check if camera permission is already granted
        Log.i(TAG, "Checking permissions")

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Permissions are granted")

            // Camera permission is already granted
            // You can perform camera-related tasks here


        } else {
            // Request camera permission
            Log.i(TAG, "Requesting permissions")

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )

        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG,"Requesting permissions")
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted
                // You can perform camera-related tasks here
                Log.i(TAG,"Permissions granted")

                mOpenCvCameraView.setCameraPermissionGranted()
            } else {
                Toast.makeText(context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                // Camera permission denied
                // You can handle the denial of camera permission here
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        Log.i(TAG,"In onCreateView")
        // Inflate the layout for this fragment
        _binding = FragmentGridScanBinding.inflate(inflater, container, false)
        val view = binding.root


        // set up open cv
        if(OpenCVLoader.initDebug()){
            Log.i(TAG,"OpenCV loaded!")
        }

        // set up camera preview
        Log.i(TAG,"Making Camera View")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG,"In onViewCreated")


        mOpenCvCameraView = binding.cameraPreview
        mOpenCvCameraView.setCameraPermissionGranted() // this is essential!!!
        mOpenCvCameraView.setUserRotation(90)
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK)
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)

        Log.i(TAG,"Setting up scan button")

        scan_button = binding.scanButton
        scan_button.setOnClickListener{ set_snap_to_true() }

        processed_preview = binding.processedPreview


    }

    fun getCameraViewList(): List<CameraBridgeViewBase?>? {
        return listOf<CameraBridgeViewBase>(mOpenCvCameraView)
    }

    public override fun onResume() {
        super.onResume()
        Log.i(TAG,"in on resume")
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, activity, mLoaderCallback)
        } else {
            Log.i(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    public override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG,"onDestroyView called")
        _binding = null

    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.i(TAG,"onCameraViewStarted called")
    }

    override fun onCameraViewStopped() {
        Log.i(TAG,"onCameraViewStopped called")

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        Log.i(TAG,"Got frame!")

        val mRgba = inputFrame!!.rgba()
        val (contours,cw_contour) = crosswordDetector.get_crossword_contour(mRgba)
        crosswordDetector.draw_crossword_contour(mRgba,contours,cw_contour)
        // crosswordDetector.process(mRgba)
        if (take_snap_shot && contours.isNotEmpty()) {
            Log.d(TAG,"Contours size ${contours.size}, Contours index $cw_contour")
            take_snap_shot = false
            set_preprocessed(contours[cw_contour],mRgba)
            // GlobalScope.launch {  }

        }
        return mRgba
    }

    fun set_preprocessed(contour: MatOfPoint, image: Mat){
        Log.d(TAG, "Setting snapshot preview image")
        val input_warp = crosswordDetector.crop_to_crossword(contour, image)
        val rect_to_crop = Rect(0, 0, 500, 500)
        val cropped = input_warp.submat(rect_to_crop)
        Log.d(TAG, "Making bitmap")
        val bitmap =
            Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(cropped, bitmap);
        val matrix = Matrix()
        matrix.postRotate(90f)
        val bitmap_rot =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        Log.d(TAG, "Displaying bitmap")

        GlobalScope.launch {
            processed_preview.updateBitmap(bitmap_rot)
        }

    }

    suspend fun ImageView.updateBitmap(bitmap: Bitmap) = withContext(Dispatchers.Main) {
        setImageBitmap(bitmap)
    }

    fun set_snap_to_true(){
        take_snap_shot = true
    }


    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(activity) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView.enableView()
                    Log.i(TAG, "enabled view successfully")

                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }
}