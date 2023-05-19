package com.example.learn_opencv

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.rotationMatrix
import androidx.drawerlayout.widget.DrawerLayout
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.learn_opencv.databinding.ActivityMainBinding
import com.example.learn_opencv.fragments.ScanFragment
import com.example.learn_opencv.fragments.SolveFragment
import com.example.learn_opencv.ui.solveScreen.PuzzleSolveViewModel
import com.example.learn_opencv.ui.solveScreen.PuzzleSolveViewModelFactory
import com.example.learn_opencv.viewModels.CrosswordScanViewModel
import com.example.learn_opencv.viewModels.CrosswordScanViewModelFactory
import com.example.learn_opencv.viewModels.PuzzleSelectViewModel
import com.example.learn_opencv.viewModels.PuzzleSelectViewModelFactory
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.io.File


class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    private val scanViewModel: CrosswordScanViewModel by viewModels {
        CrosswordScanViewModelFactory((this.application as PuzzleApplication).repository)
    }

    private val puzzleSelectViewModel: PuzzleSelectViewModel by viewModels {
        PuzzleSelectViewModelFactory((this.application as PuzzleApplication).repository)
    }

    private val puzzleSolveViewModel : PuzzleSolveViewModel by viewModels {
        PuzzleSolveViewModelFactory((this.application as PuzzleApplication).repository)
    }

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
        startOpenCV()
        Log.i(TAG, "Setting content")
        setContent {

            CrosswordApp(
                scanViewModel,
                puzzleSelectViewModel,
                puzzleSolveViewModel,
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
}