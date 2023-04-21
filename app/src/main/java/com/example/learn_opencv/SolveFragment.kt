package com.example.learn_opencv

import android.R
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.RelativeLayout
import android.widget.RelativeLayout.ALIGN_PARENT_START
import android.widget.RelativeLayout.ALIGN_PARENT_TOP
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintProperties.WRAP_CONTENT
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.learn_opencv.databinding.FragmentSolveBinding
import kotlin.properties.Delegates


class SolveFragment : Fragment() {

    private var _binding: FragmentSolveBinding? = null
    private val binding get() = _binding!!
    private val TAG = "SolveFragment"
    private val viewModel: puzzleViewModel by activityViewModels()
    private val clueBoxes = mutableMapOf<Pair<Int, Int>, toggleEditText >()
    lateinit private var activeClue : clue
    lateinit private var ansGrid : GridLayout

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

        viewModel.dimension = 4

        ansGrid = GridLayout(context)
        ansGrid.columnCount = viewModel.dimension
        ansGrid.rowCount = viewModel.dimension

        //make all the boxes
        viewModel.clueMap.forEach { coord, clueList ->
            val letterBox = letterBoxFactory(coord, clueList, ansGrid)
            clueBoxes[coord] = letterBox!!
        }

        //apply listeners to each cluebox
        viewModel.clueMap.forEach { coord, clueList ->
            clueBoxes[coord]!!.apply {
                //set the highlighter up
                this.setOnTouchListener{ view, event ->
                    if (event.getAction() == MotionEvent.ACTION_DOWN){

                        Log.d(TAG,"number of clues associated with (${coord.first},${coord.second})" +
                                " ${viewModel.clueMap[coord]!!.size}")
                        Log.d(TAG,"current select state: ${clueBoxes[coord]!!.toggled}")

                        if(clueList.size > 1) {
                            when (clueBoxes[coord]!!.toggled) {
                                toggleState.SELECT_A ->{
                                    var clueName = viewModel.clueMap[coord]!![1]
                                    activeClue = viewModel.clues[clueName]!!
                                    clueBoxes[coord]!!.toggled = toggleState.SELECT_B
                                }
                                toggleState.SELECT_B -> {
                                    var clueName = viewModel.clueMap[coord]!![0]
                                    activeClue = viewModel.clues[clueName]!!
                                    clueBoxes[coord]!!.toggled = toggleState.SELECT_A
                                }
                                toggleState.NOT_SELECT -> {
                                    var clueName = viewModel.clueMap[coord]!![0]
                                    activeClue = viewModel.clues[clueName]!!
                                    clueBoxes[coord]!!.toggled = toggleState.SELECT_A
                                }
                            }
                        }
                        else {
                            var clueName = viewModel.clueMap[coord]!![0]
                            activeClue = viewModel.clues[clueName]!!
                            clueBoxes[coord]!!.toggled = toggleState.SELECT_A
                        }
                        Log.d(TAG,"new select state: ${clueBoxes[coord]!!.toggled}")
                        Log.i(TAG,"selected: ${activeClue.clueName} , ${activeClue.clueBoxes}")
                        clueBoxes[coord]!!.activeClueName = activeClue.clueName
                        viewModel.activeClue = activeClue.clueName
                        Log.i(TAG,"set ${coord}: ${clueBoxes[coord]!!.activeClueName}")


                        //deselect all
                        clueBoxes.forEach { pair, toggleEditText ->
                            if(pair != coord ) {
                                toggleEditText.toggled = toggleState.NOT_SELECT
                                toggleEditText.setBackgroundColor(Color.LTGRAY)
                            }
                        }

                        when (clueBoxes[coord]!!.toggled) {
                            toggleState.SELECT_A -> {
                                val relatedBoxes = viewModel.clues[clueList[0]]!!.clueBoxes
                                relatedBoxes!!.forEach { it ->
                                    clueBoxes[it]!!.setBackgroundColor(Color.RED)
                                    clueBoxes[it]!!.toggled = toggleState.SELECT_A
                                }
                            }
                            else -> {
                                val relatedBoxes = viewModel.clues[clueList[1]]!!.clueBoxes
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
        }
        viewModel.clueMap.forEach { coord, clueList ->
            clueBoxes[coord]!!.apply {
                this.addTextChangedListener( object : TextWatcher {
                    fun getNextEdit() : toggleEditText{
                        var clueCoords = activeClue.clueBoxes
                        val nextBoxIdx = clueCoords.indexOf(coord)+1
                        Log.i(TAG,"curr idx ${nextBoxIdx-1}, next idx ${nextBoxIdx}")

                        if( nextBoxIdx < activeClue.clueBoxes.size){
                            Log.i(TAG,"setting next edit box")
                            return clueBoxes[clueCoords[nextBoxIdx]]!!
                        }
                        else{
                            return clueBoxes[coord]!!
                        }
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun afterTextChanged(p0: Editable?) {
                        if(!p0.isNullOrEmpty()) {
                            if(p0.length == 2) {
                                Log.i(TAG, "setting text to ${p0?.last()}")
                                //val char = p0?.last()
                                //p0.clear()
                                clueBoxes[coord]?.setText(p0?.last().toString())
                                //p0.clear()
                                clueBoxes[coord]?.setSelection(1)
                                //p0.replace(0, 1, char.toString())
                            }
                            if(p0.length == 1) {
                                Log.i(TAG, "requesting focus of next idx")
                                getNextEdit().requestFocus()
                            }

                        }

                    }

                })

            }
        }

        binding.root.addView(ansGrid)

        }


    fun letterBoxFactory(coord : Pair<Int,Int>, clueList : List<String>, ansGrid : GridLayout) : toggleEditText? {

        val row = GridLayout.spec(coord.first)
        val col = GridLayout.spec(coord.second)
        val cell = GridLayout.LayoutParams(row, col)
        cell.width = screenWidth/6
        cell.height = screenWidth/6

        val clueBox = FrameLayout(requireContext())
        val textEditParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT,Gravity.CENTER)

        val labelParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.NO_GRAVITY)
//        val editLayoutParams = FrameLayout.LayoutParams(FrameLayout.CENTER_HORIZONTAL, FrameLayout.CENTER_VERTICAL,
//        RelativeLayout.W)

        val tv = context?.let { toggleEditText(it) }
        if (tv != null) {
            tv.row = coord.first
            tv.col = coord.second
            tv.id = View.generateViewId()
            tv.setLayoutParams(textEditParams)
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundColor(Color.LTGRAY);
            tv.inputType =InputType.TYPE_CLASS_TEXT
            //tv.imeOptions = EditorInfo.IME_ACTION_NEXT
            //tv.setText("($row_idx,$col_idx)");
            tv.setTextColor(Color.GREEN)
            tv.setTextAppearance(context, R.style.TextAppearance_Large);
            tv.toggled = toggleState.NOT_SELECT
            //tv.focusable = View.NOT_FOCUSABLE
            when(clueList.size){
                1->tv.isCheckLetter = false
                2->tv.isCheckLetter = true
            }

            val label = context?.let { TextView(it) }
            if (label != null) {
                label.text = "test"
                label.setPadding(10,0,10,0)
                label.setGravity(Gravity.TOP)
                label.setLayoutParams(labelParams)
            }

            //boxLayout.width = screenWidth/6
            clueBox.addView(tv,textEditParams)
            clueBox.addView(label,labelParams)
            //ansGrid.addView(label, cell);
            ansGrid.addView(clueBox, cell);


        }
        return tv
    }
}

