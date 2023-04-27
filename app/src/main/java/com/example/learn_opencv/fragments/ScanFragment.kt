package com.example.learn_opencv.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
//import androidx.navigation.Navigation
//import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.learn_opencv.databinding.FragmentScanBinding
import com.example.learn_opencv.fragments.GridScanFragment
import com.example.learn_opencv.fragments.GridScanPreviewFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class ScanFragment : Fragment() {

    lateinit var bottomNav : BottomNavigationView
    private lateinit var navController: NavController

    lateinit var gridScanFragment: GridScanFragment
    lateinit var clueScanFragment: ClueScanFragment
    lateinit var gridScanPreviewFragment: GridScanPreviewFragment

    private val TAG = "ScanFragment"
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        Log.i(TAG, "In onCreateView")

        // Inflate the layout for this fragment
        _binding = FragmentScanBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG,"In onViewCreated")

        navController = findNavController(this)
        //navController = Navigation.findNavController(view)
        setupWithNavController(binding.bottomNav,navController)

//        gridScanFragment = GridScanFragment()
//        gridScanPreviewFragment = GridScanPreviewFragment()
//        clueScanFragment = ClueScanFragment()

//        bottomNav = binding.bottomNav
//
//        displayFragment(gridScanFragment)

//        bottomNav.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.gridScan -> {
//                    displayFragment(gridScanFragment)
//                    true
//                }
//                R.id.clueScan -> {
//                    displayFragment(clueScanFragment)
//                    true
//                }
//                R.id.previewScan -> {
//                    displayFragment(gridScanPreviewFragment)
//                    true
//                }
//                else -> { true }
//            }
//        }

    }

//    private fun displayFragment(fragment : Fragment){
//        parentFragmentManager.beginTransaction().apply {
//            replace(R.id.mainScanFragment, fragment)
//            //addToBackStack(null)
//            commit()
//        }
//    }


}



