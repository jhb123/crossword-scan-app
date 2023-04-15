package com.example.learn_opencv

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import com.example.learn_opencv.databinding.FragmentGridScanBinding
import com.example.learn_opencv.databinding.FragmentGridScanPreviewBinding


class GridScanPreviewFragment : Fragment() {

    private val TAG = "GridScanPreviewFragment"
    private var _binding: FragmentGridScanPreviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CrosswordScanViewModel by activityViewModels()

    private lateinit var cropPreview : ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.i(TAG, "In onCreateView")
        // Inflate the layout for this fragment
        _binding = FragmentGridScanPreviewBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        cropPreview = binding.cropPreview

        viewModel.getGridImgResize().observe(viewLifecycleOwner) {
            cropPreview.setImageBitmap(it)
        }

    }


}