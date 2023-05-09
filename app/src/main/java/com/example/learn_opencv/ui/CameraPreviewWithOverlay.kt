package com.example.learn_opencv.ui

import android.graphics.*
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface.ROTATION_0
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.learn_opencv.util.ImageUtils
import com.example.learn_opencv.viewModels.CrosswordScanViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors


private val TAG = "cameraWithOverlay"

@Composable
@androidx.camera.core.ExperimentalGetImage
fun CameraPreviewWithOverlay(
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    viewModel : CrosswordScanViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current



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

        Log.i(TAG,"Crop overlay dimension: ${rect.height()} x ${rect.width()}")

        canvas.drawRoundRect(
            rect, cornerRadius, cornerRadius, rectPaint
        )
        canvas.drawRoundRect(
            rect, cornerRadius, cornerRadius, outlinePaint
        )
        holder.unlockCanvasAndPost(canvas)
    }

    AndroidView(factory = { context ->

        val displayMetrics = context.getResources().getDisplayMetrics();


        val widthScaleFactor = 1.0
        val heightScaleFactor = 0.3
        val layoutWidth = (widthScaleFactor*displayMetrics.widthPixels).toInt()
        val layoutHeight = (heightScaleFactor*displayMetrics.heightPixels).toInt()
        val cameraTargetResolution = Size(900,1200)

        val roiHeight = 70 //The amount to crop away.
        val roiWidth = 10

        //this will contain the preview and the overlay
        val constraintLayout = ConstraintLayout(context).apply{
            layoutParams = ViewGroup.LayoutParams(
                layoutWidth,
                layoutHeight
            )

        }

        //define the overlay
        val overlay = SurfaceView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.setZOrderOnTop(true);
            holder.setFormat(PixelFormat.TRANSPARENT)
            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(p0: SurfaceHolder) {
                    holder?.let { drawOverlay(it, roiHeight, roiWidth) }
                }
                override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
                }
                override fun surfaceDestroyed(p0: SurfaceHolder) {
                }
            })
        }


        val previewView = PreviewView(context).apply {
            this.scaleType = PreviewView.ScaleType.FILL_START
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Preview is incorrectly scaled in Compose on some devices without this
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }


        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetResolution(cameraTargetResolution)
                .setTargetRotation(ROTATION_0)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(cameraTargetResolution)
                .setTargetRotation(ROTATION_0)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val executor = Executors.newSingleThreadExecutor()
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            imageAnalysis.setAnalyzer(executor, ImageAnalysis.Analyzer { imageProxy ->
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val mediaImage = imageProxy.image

                Log.i(TAG,"rotated  by $rotationDegrees")
                if (mediaImage != null) {

                    //default case for 0deg
                    var imageHeight = mediaImage.height
                    var imageWidth = mediaImage.width

                    when(imageProxy.imageInfo.rotationDegrees){
                        90 -> {
                            imageHeight = mediaImage.width
                            imageWidth = mediaImage.height
                        }
                        270 -> {
                            imageHeight = mediaImage.width
                            imageWidth = mediaImage.height
                        }
                    }

                    //assumes the image is cropped vertically, which I think it always will be.
                    //maybe put this as a check?

                    //find the width and height of the outline in the coordinates of the image.
                    val previewHeightPixels = imageWidth * ( layoutHeight.toDouble()/layoutWidth.toDouble() )
                    val cropRoiHeight = previewHeightPixels*((100-roiHeight)/100.0)
                    val cropRoiWidth = imageWidth*((100-roiWidth)/100.0)
                    val cropRoiStart = imageWidth*(roiWidth/200.0) //leave equal space, need factor of 2
                    val cropRoiTop = previewHeightPixels*(roiHeight/200.0)

                    //val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    val convertImageToBitmap = ImageUtils.convertYuv420888ImageToBitmap(mediaImage)

                    Log.i(TAG,"shape: $cropRoiStart, $cropRoiTop, $cropRoiWidth, $cropRoiHeight")

                    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }


                    val croppedBitmap = when(imageProxy.imageInfo.rotationDegrees){
                        0-> {
                            Bitmap.createBitmap(convertImageToBitmap,
                                cropRoiStart.toInt(), cropRoiTop.toInt(), cropRoiWidth.toInt(), cropRoiHeight.toInt())
                        }
                        90 -> {
                            Bitmap.createBitmap(convertImageToBitmap,
                                cropRoiTop.toInt(),cropRoiStart.toInt(), cropRoiHeight.toInt(),cropRoiWidth.toInt(),
                                matrix, true)
                        }
                        270 -> {
                            Bitmap.createBitmap(convertImageToBitmap,
                                cropRoiTop.toInt(),cropRoiStart.toInt(), cropRoiHeight.toInt(),cropRoiWidth.toInt(),
                                matrix, true)
                        }
                        else -> {
                            Bitmap.createBitmap(convertImageToBitmap,
                                cropRoiStart.toInt(), cropRoiTop.toInt(), cropRoiWidth.toInt(), cropRoiHeight.toInt())
                        }
                    }

                    val image = InputImage.fromBitmap(croppedBitmap, 0)

                    Log.i(TAG,"Cropped image ${image.height}x${image.width}")
                    val result = recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            val textBlocks = visionText.text
                            viewModel.clueTextDebug.postValue(textBlocks)

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

        //add both views to the constraint layout
        constraintLayout.apply {
            this.addView(overlay)
            this.addView(previewView)
        }

        constraintLayout
    })

}