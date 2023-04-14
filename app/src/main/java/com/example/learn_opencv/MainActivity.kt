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





class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    var scanFragment = ScanFragment()
    var solveFragment = SolveFragment()
    private lateinit var navController: NavController


    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        navController = Navigation.findNavController(this,R.id.mainFrame)
        //navController = Navigation.findNavController(view)
        setupWithNavController(binding.bottomNav, navController)


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

