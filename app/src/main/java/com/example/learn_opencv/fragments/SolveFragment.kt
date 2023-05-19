package com.example.learn_opencv.fragments

import android.os.Bundle
import android.view.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.fragment.app.Fragment
import com.example.learn_opencv.*
import com.example.learn_opencv.ui.solveScreen.*
import com.example.learn_opencv.ui.solveScreen.PuzzleSolveViewModel

private val TAG = "SolveFragment"

class SolveFragment : Fragment() {

    lateinit private var viewModel: PuzzleSolveViewModel //by activityViewModels{
//        PuzzleSolveViewModelFactory((requireActivity().application as PuzzleApplication).repository)
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val puzzleIdx = arguments?.getInt("puzzle_id")
        viewModel = PuzzleSolveViewModel(
            (requireActivity().application as PuzzleApplication).repository)

        //viewModel.setPuzzle(puzzleIdx!!)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {

                SolveScreen(uiState = viewModel.uiState.collectAsState(),
                onClueSelect = {viewModel.updateactiveClue(it)},
                setLetter = {viewModel.setLetter(it)},
                delLetter = {viewModel.delLetter()},
                updateCurrentCell = { viewModel.updateCurrentCell(it) },
                updateCurrentClue = {viewModel.updateactiveClue2(it)},
                cellSetFromPuzzle = {viewModel.convertPuzzleToCellSet(it)},//should this be immutable?
                labelledClues = {viewModel.getLabelledCells(it)} //should these even be functions!?
                )
            }
        }
    }
}
