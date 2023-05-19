package com.example.learn_opencv.fragments

import android.os.Build
import android.os.Bundle
import android.provider.SyncStateContract.Helpers.insert
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import com.example.learn_opencv.PuzzleApplication
import com.example.learn_opencv.databinding.FragmentGridScanPreviewBinding
import com.example.learn_opencv.ui.gridScanScreen.gridScanScreen
import com.example.learn_opencv.ui.puzzlePreviewScreen.puzzlePreviewScreen
import com.example.learn_opencv.viewModels.CrosswordScanViewModel
import com.example.learn_opencv.viewModels.CrosswordScanViewModelFactory


class GridScanPreviewFragment : Fragment() {

    private val TAG = "GridScanPreviewFragment"
    private var _binding: FragmentGridScanPreviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CrosswordScanViewModel by activityViewModels{
        CrosswordScanViewModelFactory((requireActivity().application as PuzzleApplication).repository)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            val uiGridState = viewModel.uiGridState
            val uiClueState = viewModel.uiState


            setContent {
                puzzlePreviewScreen(
                    uiGridState = uiGridState.collectAsState(),
                    uiClueState = uiClueState.collectAsState(),
                    onSave = {viewModel.insert()}
                )
            }
        }
    }

//    private lateinit var cropPreview : ImageView

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        Log.i(TAG, "In onCreateView")
//        // Inflate the layout for this fragment
//        _binding = FragmentGridScanPreviewBinding.inflate(inflater, container, false)
//        return binding.root
//
//    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)




//        cropPreview = binding.cropPreview
//
//        viewModel.getGridImgResize().observe(viewLifecycleOwner) {
//            cropPreview.setImageBitmap(it)
//        }
//
//        binding.saveButton.setOnClickListener{
//            viewModel.insert()
//        }
//
//    }


}