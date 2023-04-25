package com.example.learn_opencv.fragments

import android.R
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.learn_opencv.Clue
import com.example.learn_opencv.databinding.FragmentSolveBinding
import com.example.learn_opencv.toggleEditText
import com.example.learn_opencv.toggleState
import com.example.learn_opencv.viewModels.PuzzleSolveViewModel
import kotlin.properties.Delegates


class SolveFragment : Fragment() {

    private var _binding: FragmentSolveBinding? = null
    private val binding get() = _binding!!
    private val TAG = "SolveFragment"
    private val viewModel: PuzzleSolveViewModel by activityViewModels()
    private val clueBoxes = mutableMapOf<Pair<Int, Int>, toggleEditText>()
    lateinit private var activeClue : Clue
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

        viewModel.dimension = 15

        ansGrid = GridLayout(context)
        ansGrid.columnCount = viewModel.dimension
        ansGrid.rowCount = viewModel.dimension
        ansGrid.setBackgroundColor(getColor("gridBlack"))


        //make all the boxes
        viewModel.coordClueNamesMap.forEach { coord, clueList ->
            val letterBox = letterBoxFactory(coord, clueList, ansGrid)
            clueBoxes[coord] = letterBox!!


        }

        //apply listeners to each cluebox
        viewModel.coordClueNamesMap.forEach { coord, clueList ->
            clueBoxes[coord]!!.apply {
                //set the highlighter up
                this.setOnTouchListener{ view, event ->
                    //this.setSelection(this.text!!.lastIndex+1)
                    if (event.getAction() == MotionEvent.ACTION_DOWN){

                        Log.d(TAG,"number of clues associated with (${coord.first},${coord.second})" +
                                " ${viewModel.coordClueNamesMap[coord]!!.size}")
                        Log.d(TAG,"current select state: ${clueBoxes[coord]!!.toggled}")

                        if(clueList.size > 1) {
                            when (clueBoxes[coord]!!.toggled) {
                                toggleState.SELECT_A ->{
                                    var clueName = viewModel.coordClueNamesMap[coord]!![1]
                                    activeClue = viewModel.clues[clueName]!!
                                    clueBoxes[coord]!!.toggled = toggleState.SELECT_B
                                }
                                toggleState.SELECT_B -> {
                                    var clueName = viewModel.coordClueNamesMap[coord]!![0]
                                    activeClue = viewModel.clues[clueName]!!
                                    clueBoxes[coord]!!.toggled = toggleState.SELECT_A
                                }
                                toggleState.NOT_SELECT -> {
                                    var clueName = viewModel.coordClueNamesMap[coord]!![0]
                                    activeClue = viewModel.clues[clueName]!!
                                    clueBoxes[coord]!!.toggled = toggleState.SELECT_A
                                }
                            }
                        }
                        else {
                            var clueName = viewModel.coordClueNamesMap[coord]!![0]
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
                                toggleEditText.setBackgroundColor(getColor("nonHighlighted"))
                            }
                        }

                        when (clueBoxes[coord]!!.toggled) {
                            toggleState.SELECT_A -> {
                                val relatedBoxes = viewModel.clues[clueList[0]]!!.clueBoxes
                                relatedBoxes!!.forEach { it ->
                                    clueBoxes[it]!!.setBackgroundColor(getColor("highlighted"))
                                    clueBoxes[it]!!.toggled = toggleState.SELECT_A
                                }
                            }
                            else -> {
                                val relatedBoxes = viewModel.clues[clueList[1]]!!.clueBoxes
                                relatedBoxes!!.forEach { it ->
                                    clueBoxes[it]!!.setBackgroundColor(getColor("highlighted"))
                                    clueBoxes[it]!!.toggled = toggleState.SELECT_B
                                }
                            }
                        }

                        //view.performClick()
                        return@setOnTouchListener false
                    }
                    //view.performClick()
                    return@setOnTouchListener false
                }

                //set the text wathcher
                this.addTextChangedListener( object : TextWatcher {
                    fun getNextEditCoord() : Pair<Int,Int>{
                        var clueCoords = activeClue.clueBoxes
                        val nextBoxIdx = clueCoords.indexOf(coord)+1
                        Log.i(TAG,"curr idx ${nextBoxIdx-1}, next idx ${nextBoxIdx}")

                        if( nextBoxIdx < activeClue.clueBoxes.size){
                            Log.i(TAG,"setting next edit box")
                            return clueCoords[nextBoxIdx]
                        }
                        else{
                            return coord
                        }
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        Log.i(TAG, "before text changed for ${coord}: ${listOf(p0,p1,p2,p3)}")
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        Log.i(TAG, "on text changed for ${coord}: ${listOf(p0,p1,p2,p3)}")
                        if(clueBoxes[coord]!!.hasFocus()) {
                            if (!p0.isNullOrEmpty()) {
                                if (p0.length <= 2) {
                                    Log.i(TAG, "requesting focus of next idx")
                                    val nextEditCoord = getNextEditCoord()
                                    if (nextEditCoord != coord) {
                                        val idx = clueBoxes[nextEditCoord]!!.text?.lastIndex
                                        //Log.i(TAG, "next text edit contains: ${nextEdit.text}")
                                        Log.i(TAG, "next text edit last idx: ${idx}")
                                        clueBoxes[nextEditCoord]!!.requestFocus()
                                        //clueBoxes[nextEditCoord]!!.setSelection(idx!!+1)
                                    } else {
                                        binding.root.requestFocus()
                                    }
                                }
                                Log.i(TAG, "text at p1 ${p0!!.toString()[p1].uppercase()}")
                                Log.i(TAG, "text at p2 ${p0!!.toString()[p2].uppercase()}")

                                Log.i(TAG, "setting text to ${p0!!.toString()[p1].uppercase()}")
                                viewModel.coordTextMap[coord]!!.postValue(p0!!.toString()[p1].uppercase())
                                clueBoxes[coord]!!.setTextColor(getColor("gridBlack"))

                            }
                        }
                    }

                    override fun afterTextChanged(p0: Editable?) {
                    }
                })


                this.setOnFocusChangeListener { view, isFocussed ->
                    if(isFocussed) {
                        Log.i(TAG,"Focussed ${coord}")
                        this.setSelection(0)
                    }
                }

                this.setOnKeyListener( View.OnKeyListener { view, keyCode, keyEvent ->
                    if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.action == KeyEvent.ACTION_DOWN) {
                        if(clueBoxes[coord]!!.text.toString() == "") {
                            var clueCoords = activeClue.clueBoxes
                            val prevBoxIdx = clueCoords.indexOf(coord) - 1
                            if (prevBoxIdx >= 0) {
                                val prevClueBox = clueBoxes[clueCoords[prevBoxIdx]]!!
                                prevClueBox.requestFocus()
                                prevClueBox.setSelection(prevClueBox.text!!.lastIndex+1 )

                            }
                        }
                        return@OnKeyListener true
                    }
                    false
                })


//                this.setOnClickListener{
//                    Log.i(TAG,"Clicked ${coord}")
//                    this.setSelection(0)
//                }

            }
        }

        binding.root.addView(ansGrid)

        //activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }


    fun letterBoxFactory(coord : Pair<Int,Int>, clueList : List<String>, ansGrid : GridLayout) : toggleEditText? {

        val row = GridLayout.spec(coord.first)
        val col = GridLayout.spec(coord.second)
        val cell = GridLayout.LayoutParams(row, col)
        cell.width = screenWidth/15
        cell.height = screenWidth/15

        val clueBox = FrameLayout(requireContext())
        clueBox.setPadding(1,1,1,1)
        val textEditParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,Gravity.CENTER)

        val labelParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.NO_GRAVITY)
//        val editLayoutParams = FrameLayout.LayoutParams(FrameLayout.CENTER_HORIZONTAL, FrameLayout.CENTER_VERTICAL,
//        RelativeLayout.W)

        val tv = context?.let { toggleEditText(it) }
        if (tv != null) {
            Log.d(TAG,"setting up editText(${coord.first},${coord.second})")
            val textObserver = Observer< String> { text ->
                tv.setText(text)
                tv.setTextColor(getColor("gridBlack"))
            }
            viewModel.coordTextMap[coord]!!.observe(viewLifecycleOwner,textObserver)
            //viewModel.getCoordTextMap().observe(viewLifecycleOwner, textObserver )
            //tv.setText(viewModel.coordTextMap[coord])
            tv.row = coord.first
            tv.col = coord.second
            tv.id = View.generateViewId()
            tv.setLayoutParams(textEditParams)
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundColor(getColor("nonHighlighted"))
            tv.setPadding(0 , 0,0,0)
            tv.setTextSize(0.5f)
            tv.inputType = EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            tv.setTextAppearance(context, R.style.TextAppearance_Large);
            tv.toggled = toggleState.NOT_SELECT
            tv.setFilters(arrayOf(InputFilter.LengthFilter(2)))
            when(clueList.size){
                1->tv.isCheckLetter = false
                2->tv.isCheckLetter = true
            }

            val label = context?.let { TextView(it) }
            if (label != null && coord in viewModel.coordClueLabels) {
                label.text = viewModel.coordClueLabels[coord]
                label.setPadding(0,-10,0,0)
                label.setGravity(Gravity.TOP)
                label.setLayoutParams(labelParams)
                label.setTextColor(getColor("gridBlack"))

            }

            //boxLayout.width = screenWidth/6
            clueBox.addView(tv,textEditParams)
            clueBox.addView(label,labelParams)
            //ansGrid.addView(label, cell);
            ansGrid.addView(clueBox, cell);


        }
        return tv
    }

    fun getColor(name: String) : Int {
        return resources.getColor(resources.getIdentifier(name, "color", activity?.packageName),activity?.theme)
    }
}



