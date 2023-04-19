package com.example.learn_opencv

import android.R
import android.graphics.Color
import android.graphics.Point
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.GridLayout
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.learn_opencv.databinding.FragmentSolveBinding
import org.w3c.dom.Text
import kotlin.properties.Delegates


class SolveFragment : Fragment() {

    private var _binding: FragmentSolveBinding? = null
    private val binding get() = _binding!!
    private val TAG = "SolveFragment"
    private val viewModel: puzzleViewModel by activityViewModels()
    private val clueBoxes = mutableMapOf<Pair<Int, Int>, toggleEditText >()

    private var screenWidth by Delegates.notNull<Int>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSolveBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val size = Point()
        requireActivity().getWindowManager().getDefaultDisplay().getSize(size)
        screenWidth = size.x

        viewModel.dimension = 3

        val ansGrid = GridLayout(context)
        ansGrid.columnCount = viewModel.dimension
        ansGrid.rowCount = viewModel.dimension

        //make all the boxes
        viewModel.clueMap.forEach { coord, clueList ->
            val letterBox = addLetterBox(coord.first, coord.second, ansGrid)
            clueBoxes[coord] = letterBox!!
        }


        //apply on click listeners to each box
        viewModel.clueMap.forEach { coord, clueList ->
            clueBoxes[coord]!!.setOnTouchListener{ view, event ->
                if (event.getAction() == MotionEvent.ACTION_DOWN){

                    Log.i(TAG,"number of clues associated with (${coord.first},${coord.second})" +
                            " ${viewModel.clueMap[coord]!!.size}")
                    Log.i(TAG,"current select state: ${clueBoxes[coord]!!.toggled}")

                    if(clueList.size > 1) {
                        when (clueBoxes[coord]!!.toggled) {
                            toggleState.SELECT_A -> clueBoxes[coord]!!.toggled = toggleState.SELECT_B
                            toggleState.SELECT_B -> clueBoxes[coord]!!.toggled = toggleState.SELECT_A
                            toggleState.NOT_SELECT -> clueBoxes[coord]!!.toggled = toggleState.SELECT_A
                        }
                    }
                    else {
                        clueBoxes[coord]!!.toggled = toggleState.SELECT_A
                    }
                    Log.i(TAG,"new select state: ${clueBoxes[coord]!!.toggled}")


                    clueBoxes.forEach { pair, toggleEditText ->
                        if(pair != coord ) {
                            toggleEditText.toggled = toggleState.NOT_SELECT
                            toggleEditText.setBackgroundColor(Color.LTGRAY)
                        }
                    }

                    when (clueBoxes[coord]!!.toggled) {
                        toggleState.SELECT_A -> {
                            val relatedBoxes = viewModel.clueMap[coord]!![0]
                            relatedBoxes!!.forEach { it ->
                                clueBoxes[it]!!.setBackgroundColor(Color.RED)
                                clueBoxes[it]!!.toggled = toggleState.SELECT_A
                            }
                        }
                            else -> {
                            val relatedBoxes = viewModel.clueMap[coord]!![1]
                            relatedBoxes!!.forEach { it ->
                                clueBoxes[it]!!.setBackgroundColor(Color.RED)
                                clueBoxes[it]!!.toggled = toggleState.SELECT_B
                            }
                        }
                    }

                    view.performClick()
                    return@setOnTouchListener false
                }
                view.performClick()
                return@setOnTouchListener false
            }
        }

//        for( clue in viewModel.clues){
//            val letterBoxes = mutableListOf<toggleEditText>()
//            for( letter in clue) {
//                Log.i(TAG, "row ${letter.first} col ${letter.second} ")
//                val letterBox = addLetterBox(letter.first, letter.second, ansGrid)
//                letterBoxes.add(letterBox!!)
//            }
//            for( letterBox in letterBoxes) {
//                letterBox.setOnFocusChangeListener { view, b ->
//                    Log.i(TAG,"click edit text box (${letterBox.row},${letterBox.col})")
//                    letterBox.toggled = !letterBox.toggled
//                    for( letterBox_i in letterBoxes) {
//                        letterBox_i.toggled = letterBox.toggled
//                        if( letterBox_i.toggled){
//                            letterBox_i.setBackgroundColor(Color.LTGRAY)
//                        }
//                        else(letterBox_i.setBackgroundColor(Color.RED))
//                    }
//
//                }
//
//            }
//            for (letterBox_idx in 0..letterBoxes.size-1){
//
//                val letterBox = letterBoxes[letterBox_idx]
//                Log.i(TAG,"setting setOnEditorActionListener (${letterBox.row},${letterBox.col})")
//
////                letterBox.setOnKeyListener { v, keyCode, event ->
////                    Log.i(TAG,"keycode $keyCode")
////                    Log.i(TAG,"OnKeyListener activated, for ${letterBoxes[letterBox_idx].row},${letterBoxes[letterBox_idx].col}")
////                    if (event.action == KeyEvent.ACTION_UP && letterBox_idx < letterBoxes.size-1) {
////                        Log.i(TAG,"requesting new focus for ${letterBoxes[letterBox_idx+1].row}," +
////                                "${letterBoxes[letterBox_idx+1].col}")
////                        letterBoxes[letterBox_idx+1].requestFocus()
////                        return@setOnKeyListener true
////                    }
////                    return@setOnKeyListener false
////
////                }
//
//                letterBox.addTextChangedListener {
//                    if (letterBox_idx < letterBoxes.size-1) {
//                        Log.i(TAG,"requesting new focus for ${letterBoxes[letterBox_idx+1].row}," +
//                                "${letterBoxes[letterBox_idx+1].col}")
//                        letterBoxes[letterBox_idx+1].requestFocus()
//                    }
//                }
//            }
//        }

        binding.root.addView(ansGrid)


    }


    fun addLetterBox(row_idx: Int, col_idx: Int, ansGrid : GridLayout) : toggleEditText? {
        val row = GridLayout.spec(row_idx)
        val col = GridLayout.spec(col_idx)
        val cell = GridLayout.LayoutParams(row, col)
        cell.width = screenWidth/6
        cell.height = screenWidth/6
        val tv = context?.let { toggleEditText(it) }
        if (tv != null) {
            tv.row = row_idx
            tv.col = col_idx
            tv.id = View.generateViewId()
            tv.setLayoutParams(cell);
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundColor(Color.LTGRAY);
            tv.inputType =InputType.TYPE_CLASS_TEXT
            //tv.imeOptions = EditorInfo.IME_ACTION_NEXT
            //tv.setText("($row_idx,$col_idx)");
            tv.setTextColor(Color.GREEN)
            tv.setTextAppearance(context, R.style.TextAppearance_Large);
            tv.toggled = toggleState.NOT_SELECT
            //tv.focusable = View.NOT_FOCUSABLE
            ansGrid.addView(tv, cell);
        }
        return tv
    }



}