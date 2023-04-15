package com.example.learn_opencv

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.learn_opencv.databinding.FragmentGridScanBinding
import org.opencv.android.*
import org.opencv.core.Mat

class GridScanFragment : Fragment() , CameraBridgeViewBase.CvCameraViewListener2 {

    private val TAG = "GridScanFragment"
    private var _binding: FragmentGridScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    private lateinit var scanButton : Button
    private lateinit var cropPreview : ImageView

    private val viewModel: CrosswordScanViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        Log.i(TAG, "In onCreateView")
        // Inflate the layout for this fragment
        _binding = FragmentGridScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG,"In onViewCreated")

        // check if there are camera permissions. If there are, start the camera.
        // if not, request the permissions. If the permissions are accepted, the camera
        // will start
        mOpenCvCameraView = binding.cameraPreview

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        Log.i(TAG,"Setting up scan button")

        scanButton = binding.scanButton
        scanButton.setOnClickListener{ setSnapToTrue() }

        cropPreview = binding.cropPreview

        viewModel.getGridImg().observe(viewLifecycleOwner) {
            cropPreview.setImageBitmap(it)
        }

    }

    private fun startCamera() {
        Log.i(TAG, "Starting camera")
        mOpenCvCameraView.enableView()
        mOpenCvCameraView.setCameraPermissionGranted() // this is essential!!!
        mOpenCvCameraView.setUserRotation(90)
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK)
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG,"in on resume")
        mOpenCvCameraView.enableView()
    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView()
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
        Log.d(TAG,"Got frame!")
        //val mRgba = inputFrame!!.rgba()
        //viewModel.viewFinderImg = inputFrame!!.rgba()
        viewModel.processPreview(inputFrame!!.rgba())
        return viewModel.viewFinderImgWithContour
    }

    private fun setSnapToTrue(){
        viewModel.takeSnapshot = true
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this.context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }


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


}