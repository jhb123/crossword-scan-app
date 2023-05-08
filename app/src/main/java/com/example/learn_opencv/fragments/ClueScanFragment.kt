package com.example.learn_opencv.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import androidx.compose.material.Text
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_90
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.R
import androidx.camera.core.ViewPort.FILL_CENTER
import androidx.camera.core.ViewPort.FIT
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.map
import com.example.learn_opencv.PuzzleApplication
import com.example.learn_opencv.viewModels.CrosswordScanViewModel
import com.example.learn_opencv.viewModels.CrosswordScanViewModelFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

private const val TAG = "ClueScanFragment"

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
class ClueScanFragment : Fragment() {

    private val viewModel: CrosswordScanViewModel by activityViewModels{
        CrosswordScanViewModelFactory((requireActivity().application as PuzzleApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {

                if (allPermissionsGranted()) {
                    //startCamera()
                } else {
                    requestPermissions(
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_PERMISSIONS
                    )
                }

                //val clues = viewModel.clueText.coll

                Column() {
                    CameraPreview(viewModel = viewModel)
                    LazyColumn {
                        //items(clues) { it ->

                     //}
                    }
                //content = viewModel.clueText.value)
                    //Box(modifier = Modifier.size(width = 200.dp, height = 200.dp))
                }



            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                //startCamera()
            } else {
                Toast.makeText(this.context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
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

@Composable
@androidx.camera.core.ExperimentalGetImage
fun CameraPreview(
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FIT_CENTER,
    viewModel : CrosswordScanViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current


    AndroidView(factory = { context ->

        val constraintLayout = ConstraintLayout(context)

        val overlay = SurfaceView(context).apply {
            this.setZOrderOnTop(true);
            holder.setFormat(PixelFormat.TRANSPARENT)
            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(p0: SurfaceHolder) {
                    holder?.let { drawOverlay(it, 50, 50) }
                }
                override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
                }
                override fun surfaceDestroyed(p0: SurfaceHolder) {
                }
            })
        }


        val previewView = PreviewView(context).apply {
            this.scaleType = scaleType
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Preview is incorrectly scaled in Compose on some devices without this
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }


        constraintLayout.apply {
            this.addView(overlay)
            this.addView(previewView)
        }

            //previewView.bringChildToFront(overlay)
            //drawOverlay(previewView.,)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder()
                    .setTargetResolution(Size(800, 600))
                .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(800, 600))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val executor = Executors.newSingleThreadExecutor()
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                imageAnalysis.setAnalyzer(executor, ImageAnalysis.Analyzer { imageProxy ->
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    // insert your code here.
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                        image.mediaImage?.cropRect = Rect(0,100,100,0)

                        val result = recognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                val textBlocks = visionText.textBlocks
                                //viewModel.clueText.

//                                textBlocks.forEach{
//                                    Log.i(TAG,"${it.text}")
//                                    //val bb = it.boundingBox
//                                    //Text(text = it.text)
//                                }
                            }
                            .addOnFailureListener { e ->
                                // Task failed with an exception
                                // ...
                            }

                        // Pass image to an ML Kit Vision API
                        // ...
                    }

                    // after done, release the ImageProxy object
                    imageProxy.close()
                })

                //val viewPort =  ViewPort.Builder(Rational(10, 1), ROTATION_0).build()

                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageAnalysis)
                    //.setViewPort(viewPort)
                    .build()

                try {
                    // Must unbind the use-cases before rebinding them.
                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, useCaseGroup
                    )

                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }



            }, ContextCompat.getMainExecutor(context))

        constraintLayout
        })

}

fun drawOverlay(
    holder: SurfaceHolder,
    heightCropPercent: Int,
    widthCropPercent: Int
) {
    val canvas = holder.lockCanvas()
    val bgPaint = Paint().apply {
        alpha = 140
    }
    canvas.drawPaint(bgPaint)
    val rectPaint = Paint()
    rectPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    rectPaint.style = Paint.Style.FILL
    rectPaint.color = android.graphics.Color.WHITE
    val outlinePaint = Paint()
    outlinePaint.style = Paint.Style.STROKE
    outlinePaint.color = android.graphics.Color.WHITE
    outlinePaint.strokeWidth = 4f
    val surfaceWidth = holder.surfaceFrame.width()
    val surfaceHeight = holder.surfaceFrame.height()

    val cornerRadius = 25f
    // Set rect centered in frame
    val rectTop = surfaceHeight * heightCropPercent / 2 / 100f
    val rectLeft = surfaceWidth * widthCropPercent / 2 / 100f
    val rectRight = surfaceWidth * (1 - widthCropPercent / 2 / 100f)
    val rectBottom = surfaceHeight * (1 - heightCropPercent / 2 / 100f)
    val rect = RectF(rectLeft, rectTop, rectRight, rectBottom)
    canvas.drawRoundRect(
        rect, cornerRadius, cornerRadius, rectPaint
    )
    canvas.drawRoundRect(
        rect, cornerRadius, cornerRadius, outlinePaint
    )
    val textPaint = Paint()
    textPaint.color = android.graphics.Color.WHITE
    textPaint.textSize = 50F

//        val overlayText = getString(R.string.overlay_help)
//        val textBounds = Rect()
//        textPaint.getTextBounds(overlayText, 0, overlayText.length, textBounds)
//        val textX = (surfaceWidth - textBounds.width()) / 2f
//        val textY = rectBottom + textBounds.height() + 15f // put text below rect and 15f padding
//        canvas.drawText(getString(R.string.overlay_help), textX, textY, textPaint)
    holder.unlockCanvasAndPost(canvas)
}

