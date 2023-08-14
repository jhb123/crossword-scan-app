package com.jhb.crosswordScan.ui.puzzleScanningScreens.gridScanScreen

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhb.crosswordScan.R
import com.jhb.crosswordScan.ui.puzzleScanningScreens.CrosswordScanViewModel
import com.jhb.crosswordScan.ui.puzzleScanningScreens.CrosswordScanViewModelFactory
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCamera2View
import org.opencv.core.Mat

private const val TAG = "gridScanComposable"

@Composable
fun gridScanScreen(
    scanViewModel: CrosswordScanViewModel = viewModel(
        factory = CrosswordScanViewModelFactory(),
        viewModelStoreOwner = (LocalContext.current as ComponentActivity)
    )
){

    val uiState by scanViewModel.uiGridState.collectAsState()

    val openCVlogic = OpenCVlogic(scanViewModel)
    //openCVlogic.onCameraViewStarted(800,1200)

    gridScanComposable(
        uiState = uiState,
        openCVlogic = openCVlogic,
        takePic = {scanViewModel.setSnapShotTrue() },
        clearScan = {scanViewModel.clearScan()},
    )

}

@Composable
fun gridScanComposable(
    uiState: GridScanUiState,
    //viewModel: CrosswordScanViewModel,
    openCVlogic: OpenCVlogic?,
    takePic : ()->Unit,
    clearScan : ()->Unit,
    ) {

    // made openCVlogic nullable so that the layout can be previewed
    var takingPic by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            //.padding(5.dp)
            //.background(MaterialTheme.colorScheme.background)

    )
    {
        Box() {
            AndroidView(
                modifier = Modifier
                    .width(400.dp)
                    .height(400.dp)
//                .aspectRatio((uiState.previewWidth/uiState.previewHeight).toFloat())

                ,
                factory = { context ->
                    val mOpenCvCameraView =
                        JavaCamera2View(context, CameraBridgeViewBase.CAMERA_ID_BACK)
                    mOpenCvCameraView.setCameraPermissionGranted() // this is essential!!!
                    mOpenCvCameraView.setCvCameraViewListener(openCVlogic)
                    mOpenCvCameraView.enableView()
                    mOpenCvCameraView.setUserRotation(90)
                    mOpenCvCameraView.setMaxFrameSize(1600, 1600)
                    mOpenCvCameraView
                }
            )

            val alpha: Float by animateFloatAsState(
                targetValue = if (takingPic) 1f else 0f,
                animationSpec = keyframes {
                    durationMillis = 200
                    0f at 200
                },
                finishedListener = {takingPic = false}
            )

            Box(modifier = Modifier
                .matchParentSize()
                .alpha(alpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.tertiary),
                    )
                )
            ){}
        }
        Box(
            modifier = Modifier
                .fillMaxSize(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
                //.background(MaterialTheme.colorScheme.background)
            ) {
                Card(
                    modifier = Modifier
                        //.width(200.dp)
                        .fillMaxHeight(0.8f)
                        .aspectRatio(1f)
                        .padding(5.dp)
                    //.background(MaterialTheme.colorScheme.secondary)
                ) {
                    uiState.gridPicProcessed.let {
                        if (it != null) {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = stringResource(id = R.string.contentDesc_gridImage),
                                modifier = Modifier
                                    .padding(5.dp)
                                    .fillMaxSize()
                            )
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .padding(5.dp)
                        .width(200.dp)
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        //onClick = { viewModel.takeSnapshot = true }
                        onClick = clearScan,
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_baseline_delete_24),
                            contentDescription = stringResource(id = R.string.action_delete_scan),
                        )
                    }
                    IconButton(
                        //onClick = { viewModel.takeSnapshot = true }
                        onClick = {
                            takingPic = true
                            takePic()
                                  },
                        colors = IconButtonDefaults.filledIconButtonColors(),
                        //modifier = Modifier
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_baseline_camera_24),
                            contentDescription = stringResource(id = R.string.action_photograph),
                        )
                    }
                }
            }
        }
    }
}


@Preview(widthDp = 400, heightDp = 650)
@Composable
fun GridScanPreview() {
    val uiState = GridScanUiState(
        gridPicDebug=null,
        gridPicProcessed=null
    )

    gridScanComposable(
        uiState = uiState,
        openCVlogic = null,
        takePic = {},
        clearScan = {},
    )
}

class OpenCVlogic(viewModel: CrosswordScanViewModel) :
    CameraBridgeViewBase.CvCameraViewListener2 {

    val viewModel = viewModel

    override fun onCameraViewStarted(width: Int, height: Int) {
        viewModel.openCVCameraSize(width, height)
        Log.i(TAG, "onCameraViewStarted: width = $width, height: $height")
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
