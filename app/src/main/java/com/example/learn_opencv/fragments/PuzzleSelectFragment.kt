package com.example.learn_opencv

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learn_opencv.adapters.PuzzleCardAdapter
import com.example.learn_opencv.data.PuzzleData
import com.example.learn_opencv.databinding.FragmentPuzzleSelectBinding
import com.example.learn_opencv.ui.puzzleSelectionScreen.puzzleSelectionComposable
import com.example.learn_opencv.viewModels.*


class puzzleSelectFragment : Fragment() {

    private val TAG = "puzzleSelectFragment"
    private var _binding: FragmentPuzzleSelectBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PuzzleSelectViewModel by activityViewModels {
        PuzzleSelectViewModelFactory((requireActivity().application as PuzzleApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                puzzleSelectionComposable(
                    uiState = viewModel.uiState.collectAsState(),
                    navigateToPuzzle = { -1 }
                )
            }

        }
    }
}

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        Log.i(TAG,"In onViewCreated")
//
//        super.onViewCreated(view, savedInstanceState)
//
//        //val dataset = listOf<PuzzleData>()
//        val recyclerView = binding.recyclerView
//        val adapter = PuzzleCardAdapter(context)
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(context)
//
//        //recyclerView.addOnItemTouchListener
//
//        viewModel.allPuzzles.observe(viewLifecycleOwner, Observer{puzzles ->
//            Log.i(TAG,"adding puzzle to recycler view")
//            puzzles?.let { adapter.submitList(it) }
//
//        })
//
//    }


