package com.jhb.crosswordScan.ui.gridScanScreen

import android.util.Log
import android.view.SurfaceView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jhb.crosswordScan.viewModels.CrosswordScanViewModel
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCamera2View
import org.opencv.core.Mat

private const val TAG = "gridScanComposable"
@Composable
fun gridScanScreen(
    uiState: State<GridScanUiState>,
    viewModel: CrosswordScanViewModel
    ) {

    val backgroundColour = MaterialTheme.colorScheme.background.toArgb() // this is for the cameraView


    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(5.dp)
    )
    {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth(1f)
                .height(400.dp)
                .background(MaterialTheme.colorScheme.inverseSurface),
            factory = { context ->
                val mOpenCvCameraView =
                    JavaCamera2View(context, CameraBridgeViewBase.CAMERA_ID_BACK)
                mOpenCvCameraView
            },
            update = { mOpenCvCameraView ->
                val openCVlogic = OpenCVlogic(viewModel)
                mOpenCvCameraView.enableView()
                mOpenCvCameraView.setCameraPermissionGranted() // this is essential!!!
                mOpenCvCameraView.setUserRotation(90)
                mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK)
                mOpenCvCameraView.visibility = SurfaceView.VISIBLE
                mOpenCvCameraView.setCvCameraViewListener(openCVlogic)
                //mOpenCvCameraView.clipToOutline = true
                //mOpenCvCameraView.setMaxFrameSize(1600,900)
                //mOpenCvCameraView.setBackgroundColor(backgroundColour)
                //mOpenCvCameraView.setMaxFrameSize(200,200)
            },
        )

        Card(
            modifier = Modifier
            .width(200.dp)
            .height(200.dp)
            .padding(5.dp)
            //.background(MaterialTheme.colorScheme.secondary)
        ){
            uiState.value.gridPicProcessed.let {
                if (it != null) {
                    Image(bitmap = it.asImageBitmap(),
                        contentDescription = "scanned bitmap",
                    modifier = Modifier.padding(5.dp).fillMaxSize())
                }
            }
        }


        //Row(modifier = Modifier.padding(5.dp)) {
            Button(onClick = { viewModel.takeSnapshot = true }) {
                Text("Scan")
            }
        //}
    }

}


class OpenCVlogic(viewModel: CrosswordScanViewModel) :
    CameraBridgeViewBase.CvCameraViewListener2 {

    val viewModel = viewModel

    override fun onCameraViewStarted(width: Int, height: Int) {
        viewModel.openCVCameraSize(width, height)
        Log.i(TAG, "onCameraViewStarted called")
    }

    override fun onCameraViewStopped() {
        Log.i(TAG, "onCameraViewStopped called")
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        viewModel.processPreview(inputFrame!!.rgba())
        return viewModel.viewFinderImgWithContour
    }

}