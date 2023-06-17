package com.jhb.crosswordScan

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.rotationMatrix
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.jhb.crosswordScan.data.Session
import com.jhb.crosswordScan.viewModels.CrosswordScanViewModel
import com.jhb.crosswordScan.viewModels.CrosswordScanViewModelFactory
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.io.File


class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    //val test = (this.application as PuzzleApplication).userRepository
//    private val authViewModel: AuthViewModel by viewModels {
//        AuthViewModelFactory(
//            (this.application as PuzzleApplication).userRepository
//        )
//    }

//    private val registrationViewModel:  RegistrationViewModel by viewModels {
//        RegistrationViewModelFactory(
//            (this.application as PuzzleApplication).userRepository
//        )
//    }

    private val scanViewModel: CrosswordScanViewModel by viewModels {
        CrosswordScanViewModelFactory((this.application as PuzzleApplication).repository)
    }
//
//    private val puzzleSelectViewModel: PuzzleSelectViewModel by viewModels {
//        PuzzleSelectViewModelFactory((this.application as PuzzleApplication).repository)
//    }

//    private val puzzleSolveViewModel : PuzzleSolveViewModel by viewModels {
//        PuzzleSolveViewModelFactory((this.application as PuzzleApplication).repository)
//    }


    private var latestTmpUri: Uri? = null

    private val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            val bitmap: Bitmap? = null
            val contentResolver: ContentResolver = this.contentResolver

            latestTmpUri?.let { uri ->
                var rotationMatrix = rotationMatrix(0f,0f,0f)
                val exif = contentResolver.openInputStream(uri)?.let { ExifInterface(it) }
                if(exif != null){
                    Log.i(TAG,"input image rotation ${exif.rotationDegrees.toFloat()}")
                    rotationMatrix = rotationMatrix(exif.rotationDegrees.toFloat(),0f,0f)
                }
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val rotatedBitmap = Bitmap.createBitmap(bitmap,0,0, bitmap.width,bitmap.height,rotationMatrix,true)
                //viewModel.updateCluePicDebug(rotatedBitmap)
                scanViewModel.setCluePicDebug(rotatedBitmap)
            }
        }
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val cacheDir = this.getCacheDir()
        val tmpFile = File.createTempFile("tmp_image_file", ".bmp", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(this, this.packageName +  ".provider", tmpFile)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!allPermissionsGranted()){
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        startOpenCV()
        Log.i(TAG, "Setting content")

        Session.setContext(this.applicationContext)
        Session.readSession()



        setContent {
            CrosswordApp(
                scanViewModel,
                //puzzleSelectViewModel,
                //authViewModel,
                //registrationViewModel,
                (this.application as PuzzleApplication).repository,
                takeImage = { takeImage() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "in on resume")
        startOpenCV()
    }

    private fun startOpenCV() {
        Log.i(TAG, "Starting OpenCV")
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            Log.i(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            Log.i(TAG, "using baseloarder callback")
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")

                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (CAMERA)
                .apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this.application, it) == PackageManager.PERMISSION_GRANTED
    }

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
                this?.finish()
            }
        }
    }




}