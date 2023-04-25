package com.example.learn_opencv

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.learn_opencv.databinding.ActivityMainBinding
import com.example.learn_opencv.fragments.ScanFragment
import com.example.learn_opencv.fragments.SolveFragment
import com.example.learn_opencv.viewModels.CrosswordScanViewModel
import com.example.learn_opencv.viewModels.CrosswordScanViewModelFactory
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    var scanFragment = ScanFragment()
    var solveFragment = SolveFragment()
    private lateinit var navController: NavController


    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private val viewModel: CrosswordScanViewModel by viewModels {
        CrosswordScanViewModelFactory((application as PuzzleApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startOpenCV()

        Log.i(TAG, "inflating layout")
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.i(TAG, "Finding Nav controller")
        navController = Navigation.findNavController(this, R.id.mainFrame)
        //navController = Navigation.findNavController(view)
        Log.i(TAG, "setting bottom navbar up")
        setupWithNavController(binding.bottomNav, navController)


    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "in on resume")
        startOpenCV()
    }

    private fun startOpenCV() {
        Log.i(TAG, "Starting OpenCV")
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            Log.i(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            Log.i(TAG, "using baseloarder callback")
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")

                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }
}