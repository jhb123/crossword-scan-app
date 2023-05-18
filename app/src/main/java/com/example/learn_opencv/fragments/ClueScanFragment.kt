package com.example.learn_opencv.fragments

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntRect
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.rotationMatrix
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.learn_opencv.PuzzleApplication
import com.example.learn_opencv.viewModels.CrosswordScanViewModel
import com.example.learn_opencv.viewModels.CrosswordScanViewModelFactory
import java.io.File
import kotlin.math.max
import kotlin.math.min
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.learn_opencv.ui.clueScanScreen.ClueScanScreen


private const val TAG = "ClueScanFragment"
const val REQUEST_IMAGE_CAPTURE = 1


@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
class ClueScanFragment : Fragment() {

    private val viewModel: CrosswordScanViewModel by activityViewModels {
        CrosswordScanViewModelFactory((requireActivity().application as PuzzleApplication).repository)
    }

    private var latestTmpUri: Uri? = null


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            val uiState = viewModel.uiState

            setContent {
                ClueScanScreen(
                    uiState = uiState.collectAsState(),
                    setClueScanDirection = {viewModel.setClueScanDirection(it)},
                    takeImage = { takeImage() },
                    onDragStart = { viewModel.resetClueHighlightBox(it) } ,
                    onDrag = { viewModel.changeClueHighlightBox(it) },
                    setCanvasSize = {viewModel.setCanvasSize(it)},
                    scanClues = {viewModel.scanClues()},
                )
            }
        }
    }

    private val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            val bitmap: Bitmap? = null
            val contentResolver: ContentResolver = requireContext().contentResolver

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
                viewModel.setCluePicDebug(rotatedBitmap)
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
        val cacheDir = requireContext().getCacheDir()
        val tmpFile = File.createTempFile("tmp_image_file", ".bmp", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(requireContext(), context?.applicationContext?.packageName +  ".provider", tmpFile)
    }
}


