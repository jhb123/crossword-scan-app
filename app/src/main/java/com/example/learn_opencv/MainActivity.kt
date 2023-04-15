package com.example.learn_opencv

import android.graphics.ColorSpace.match
import android.os.Bundle
import android.text.TextUtils.replace
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.learn_opencv.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startOpenCV()

        Log.i(TAG,"inflating layout")
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.i(TAG,"Finding Nav controller")
        navController = Navigation.findNavController(this,R.id.mainFrame)
        //navController = Navigation.findNavController(view)
        Log.i(TAG,"setting bottom navbar up")
        setupWithNavController(binding.bottomNav, navController)


    }

        override fun onResume() {
            super.onResume()
            Log.i(TAG,"in on resume")
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
//        drawerLayout = findViewById(R.id.mainDrawerLayout)
//        //programmatically add the toggle
//        actionBarDrawerToggle = ActionBarDrawerToggle(
//            this, drawerLayout, R.string.nav_open, R.string.nav_close
//        )
//        actionBarDrawerToggle.syncState()
//
//        //make it appear
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//
//        binding.apply{
//
//            navView.setNavigationItemSelectedListener {
//                when (it.itemId) {
//                    R.id.scan -> {
//                        displayFragment(scanFragment)
//                        true
//                    }
//                    R.id.solve -> {
//                        displayFragment(solveFragment)
//                        true
//                    }
//
//                    else ->  true
//                }
//            }

        }


//        supportFragmentManager.beginTransaction().apply {
//            replace(R.id.mainFrame, scanFragment)
//            commit()
//        }
//
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
//            true
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    private fun displayFragment(fragment : Fragment) {
//        supportFragmentManager.beginTransaction().apply {
//            replace(R.id.mainFrame, fragment)
//            addToBackStack(null)
//           commit()
//       }
//    }
//}

