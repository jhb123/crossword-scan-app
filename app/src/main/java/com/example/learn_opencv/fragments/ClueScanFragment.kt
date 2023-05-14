package com.example.learn_opencv.fragments

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Half.toFloat
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
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawStyle
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
import androidx.core.graphics.scaleMatrix
import androidx.core.graphics.times
import androidx.core.graphics.translationMatrix
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.learn_opencv.PuzzleApplication
import com.example.learn_opencv.viewModels.CrosswordScanViewModel
import com.example.learn_opencv.viewModels.CrosswordScanViewModelFactory
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


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
            setContent {


                val clueText = viewModel.currentClueText
                val currentClue = viewModel.currentClueName
                val puzzle = viewModel.puzzle
                val rawText = viewModel.clueTextRaw
                val cluePicDebug = viewModel.cluePicDebug.observeAsState()
                val croppedCluePic = viewModel.croppedCluePic.observeAsState()

                //val puzzle = viewModel.puzzle.observeAsState()

                if (allPermissionsGranted()) {
                    //startCamera()
                } else {
                    requestPermissions(
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_PERMISSIONS
                    )
                }

                val context = LocalContext.current
                val points = remember { mutableStateListOf<Offset>() }
                val isScrolling = remember {mutableStateOf(true)}
                val imageScale = remember{ mutableStateOf(1f) }
                val imageOffset = remember{ mutableStateOf(Offset(0f,0f)) }

                val displayMetrics = DisplayMetrics()
                Log.i(TAG,"Obtaining display metrics")
                requireActivity().baseContext.display?.getMetrics(displayMetrics)

                var maxDimension = Size(0f,0f)
                val editAreaWidth = 300 //pixels //displayMetrics.xdpi-30  // /displayMetrics.density
                val editAreaHeight = 400

                val (ScreenHeight, ScreenWidth) = LocalConfiguration.current.run { screenHeightDp.dp to screenWidthDp.dp }

                Log.i(TAG, "display density: ${displayMetrics.density}," +
                        " ${displayMetrics.xdpi}, ${displayMetrics.ydpi}, ${displayMetrics.densityDpi}, ${displayMetrics.scaledDensity}")
                Log.i(TAG,"edit area dp: $editAreaWidth x $editAreaHeight")

                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background))
                {

                    Row(
                        modifier = Modifier
                            .width(editAreaWidth.dp)
                            .height(editAreaHeight.dp)){
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.inverseSurface)
                            .clip(RectangleShape)
                            ) {
                            cluePicDebug.value?.asImageBitmap()?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = "Image of Clues",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .graphicsLayer(
                                            scaleX = imageScale.value,
                                            scaleY = imageScale.value,
                                            translationX = imageOffset.value.x,
                                            translationY = imageOffset.value.y,
                                        )
                                )

                            }
                            Canvas(modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(isScrolling.value) {
                                    if (isScrolling.value) {
                                        detectDragGestures(
                                            onDragStart = {
                                                points.clear()
                                                points.add(it)
                                            },
                                            onDrag = { change, _ ->
                                                points.add(change.position)
                                            }
                                        )
                                    } else {
                                        detectTransformGestures { centroid, pan, zoom, rotation ->
                                            val oldZoom = imageScale.value
                                            // imageScale.value = zoom * oldZoom // can't get this working properly.
                                            val oldCentre = imageOffset.value
                                            imageOffset.value = Offset(
                                                oldCentre.x + pan.x,
                                                oldCentre.y + pan.y
                                            )
                                        }
                                    }
                                }

                            ) {
                                maxDimension = this.size
                                Log.i(TAG,"Canvas Size $maxDimension")
                                if(points.toList().size > 0){
                                    val x1 = min(points.toList().first().x,points.toList().last().x)
                                    val y1 = min(points.toList().first().y,points.toList().last().y)
                                    val x2 = max(points.toList().first().x,points.toList().last().x)
                                    val y2 = max(points.toList().first().y,points.toList().last().y)

                                    val rectPaint = Paint()
                                    rectPaint.style = Paint.Style.FILL
                                    rectPaint.color = android.graphics.Color.WHITE
                                    val outlinePaint = Paint()
                                    outlinePaint.style = Paint.Style.STROKE
                                    outlinePaint.color = android.graphics.Color.WHITE
                                    outlinePaint.strokeWidth = 4f

                                    drawRect(
                                        topLeft = Offset(x1, y1),
                                        size = Size((x2 - x1), (y2 - y1)),
                                        alpha = 0.3f,
                                        color = White,
                                    )
                                    drawRect(
                                        topLeft = Offset(x1, y1),
                                        size = Size((x2 - x1), (y2 - y1)),
                                        style = Stroke(width = 6.dp.toPx()),
                                        color = White,

                                    )
                                    Log.i(TAG, "rectangle drawn using x1: $x1, x2: $x2, y1: $y1, y2: $y2")
                                    Log.i(TAG,"rectangle offset at ${Offset(x1, y1)}")
                                    Log.i(TAG,"rectangle size at ${Size((x2 - x1), (y2 - y1))}")


                                }

                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(1f)
                    ) {
                        // Enable this button to enable moving the background around
//                        Button(onClick = { isScrolling.value =  !(isScrolling.value)}) {
//                            if (isScrolling.value){
//                                Text("scroll")
//                            }
//                            else{
//                                Text("Set area")
//                            }
//                        }
//                        Button(onClick = {
//                            imageScale.value = 1f
//                            imageOffset.value = Offset(0f,0f)
//                        }) {
//                            Text("Reset")
//                        }
                        Button(onClick = { takeImage() }) {
                            Text("Take Picture")
                        }
                        Button(onClick = {
                            viewModel.cluePicDebug.value?.let { originalImage ->
                                Log.i(TAG,"maxDimension $maxDimension")

                                var x1 = 0f
                                var x2 = 0f//originalImage.width.toFloat()
                                var y1 = 0f
                                var y2 = 0f//originalImage.height.toFloat()

                                val widthFactor = originalImage.width/maxDimension.width
                                val heightFactor = originalImage.height/maxDimension.height
                                Log.i(TAG,"widthFactor $widthFactor, heightFactor $heightFactor")

                                if (points.toList().size > 1){
                                    // this is overly complex because I thought I'd be able to zoom as well.
                                    val leftDistance = (min(points.toList().first().x,points.toList().last().x) - imageOffset.value.x)*widthFactor
                                    val rightDistance = (originalImage.width.toFloat() - (max(points.toList().first().x, points.toList().last().x) - imageOffset.value.x) * widthFactor)

                                    val topDistance = (min(points.toList().first().y,points.toList().last().y) - imageOffset.value.y)*heightFactor
                                    val bottomDistance = (originalImage.height.toFloat() - (max(points.toList().first().y, points.toList().last().y) - imageOffset.value.y) * heightFactor)

                                    x1 = leftDistance
                                    x2 = rightDistance
                                    y1 = topDistance
                                    y2 = bottomDistance

                                }
                                if(x1 < 0){
                                    x1 = 0f
                                }
                                if(y1 < 0){
                                    y1 = 0f
                                }
                                if(x2 < 0){
                                    x2 = 0f
                                }
                                if(y2 < 0){
                                    y2 = 0f
                                }

                                Log.i(TAG, "rectangle drawn using x1: $x1, x2: $x2, y1: $y1, y2: $y2")
                                Log.i(TAG,"rectangle offset at ${Offset(x1, y1)}")
                                Log.i(TAG,"rectangle size at ${Size(originalImage.width.toFloat() - x1 - x2,
                                    originalImage.height.toFloat() - y1 - y2)}")

                               
                                val cropRect = Rect(
                                    Offset(x1, y1),
                                    Size(originalImage.width.toFloat() - x1 - x2,
                                        originalImage.height.toFloat() - y1 - y2)
                                ).roundToIntRect()


                                viewModel.croppedCluePic.value = Bitmap.createBitmap(
                                    originalImage, cropRect.left, cropRect.top, cropRect.width ,cropRect.height)
                            }

                        }) {
                            Text("Scan")
                        }
                    }

                    croppedCluePic.value?.asImageBitmap()
                        ?.let {
                            Image(
                                bitmap = it,
                                contentDescription = "Image of cropped clues",
                                modifier = Modifier
                                    .width(400.dp)
                                    .height(400.dp))
                        }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                //startCamera()
            } else {
                Toast.makeText(
                    this.context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                activity?.finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
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
                viewModel.updateCluePicDebug(rotatedBitmap)
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

