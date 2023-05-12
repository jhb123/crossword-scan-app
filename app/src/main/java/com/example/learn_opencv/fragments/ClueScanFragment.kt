package com.example.learn_opencv.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.learn_opencv.PuzzleApplication
import com.example.learn_opencv.ui.CameraPreviewWithOverlay
import com.example.learn_opencv.viewModels.CrosswordScanViewModel
import com.example.learn_opencv.viewModels.CrosswordScanViewModelFactory
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.learn_opencv.Clue

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


                val debugText = viewModel.clueTextDebug.observeAsState()
                val currentClue = viewModel.currentClue
                val puzzle = viewModel.puzzle

                //val puzzle = viewModel.puzzle.observeAsState()

                if (allPermissionsGranted()) {
                    //startCamera()
                } else {
                    requestPermissions(
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_PERMISSIONS
                    )
                }

                //val clues = viewModel.clueText.coll

                Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
                    CameraPreviewWithOverlay(viewModel = viewModel)
                    clueScanPreview(debugText.value,puzzle.value.clues,currentClue)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(15.dp),){
                        items(viewModel.puzzle.value.clues.toList()){ item ->
                            clueTextScanList(item.first, currentClue.value, setActive = {
                                viewModel.setActiveClue(it)
                            })
                        }
                    }
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
fun clueTextScanList(clueName : String, currentClueName: String, setActive: (String) -> Unit ){

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                if (clueName == currentClueName) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                shape = RoundedCornerShape(10.dp)
            )
            .pointerInput(clueName) {
                detectTapGestures {
                    setActive(clueName)
                }
        }
    ) {
        Text(clueName,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(50.dp)
                .height(100.dp)
        )
    }
}

@Composable
fun clueScanPreview(scannedText: String?, clues : Map<String , Clue>, currentClueName : State<String>){

    Box(modifier = Modifier
        .height(200.dp)
        .fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.height(160.dp)){
                Text(currentClueName.value, fontSize = 40.sp, fontWeight =  FontWeight.Bold)
                if (scannedText != null) {
                    Text(scannedText, fontSize = 20.sp)
                }
            }
            Row(){
                Button(onClick = { /**/ }){Text("left")}
                Button(onClick = { /**/ }){Text("right")}
            }
        }
    }
}