package com.example.learn_opencv

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.learn_opencv.adapters.PuzzleCardAdapter
import com.example.learn_opencv.databinding.FragmentGridScanPreviewBinding
import com.example.learn_opencv.databinding.FragmentPuzzleSelectBinding
import com.example.learn_opencv.viewModels.puzzleViewModel
import com.example.learn_opencv.viewModels.puzzleViewModelFactory


class puzzleSelectFragment : Fragment() {

    private val TAG = "puzzleSelectFragment"
    private var _binding: FragmentPuzzleSelectBinding? = null
    private val binding get() = _binding!!

    private val viewModel: puzzleViewModel by activityViewModels{
        puzzleViewModelFactory((requireActivity().application as PuzzleApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG,"In onCreateView")
        // Inflate the layout for this fragment
        _binding = FragmentPuzzleSelectBinding.inflate(inflater, container, false)
        return binding.root
        //return inflater.inflate(R.layout.fragment_puzzle_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG,"In onViewCreated")

        super.onViewCreated(view, savedInstanceState)

        //val dataset = listOf<PuzzleData>()
        val recyclerView = binding.recyclerView
        val adapter = PuzzleCardAdapter(context)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.allPuzzles.observe(viewLifecycleOwner, Observer{puzzles ->
            Log.i(TAG,"adding puzzle to recycler view")
            puzzles?.let { adapter.submitList(it) }

        })

    }


}