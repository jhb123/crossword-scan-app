package com.jhb.crosswordScan.util

import android.util.Log
import com.jhb.crosswordScan.data.Clue
import com.jhb.crosswordScan.data.Puzzle
import org.opencv.core.Core.BORDER_CONSTANT
import org.opencv.core.Core.ROTATE_180
import org.opencv.core.Core.add
import org.opencv.core.Core.bitwise_and
import org.opencv.core.Core.bitwise_not
import org.opencv.core.Core.countNonZero
import org.opencv.core.Core.rotate
import org.opencv.core.Core.subtract
import org.opencv.core.CvType.CV_32F
import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.abs

private const val TAG = "CrosswordDetector"


fun drawCrosswordContour(input_image: Mat?, contours: List<MatOfPoint>?, cw_contour: Int) {
    Imgproc.drawContours(input_image, contours, cw_contour, Scalar(0.0, 255.0, 0.0), 5)
}

fun getCrosswordContour(input_image: Mat?): Pair<List<MatOfPoint>, Int> {
    val contours: List<MatOfPoint> = ArrayList()

    //val input_image = rgbaImage
    val image = input_image?.clone()
    val dilation = Mat.ones(5, 5, Imgproc.COLOR_BGR2GRAY)
    val mHierarchy = Mat()
    Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY)
    Imgproc.GaussianBlur(image, image, Size(7.0, 7.0), 1.0)
    Imgproc.Canny(image, image, 50.0, 200.0)
    Imgproc.dilate(image, image, dilation)
    Imgproc.findContours(
        image,
        contours,
        mHierarchy,
        Imgproc.RETR_LIST,
        Imgproc.CHAIN_APPROX_SIMPLE
    )
    //Imgproc.drawContours(input_image,contours,-1, Scalar(0.0, 255.0, 0.0), 4)

    Log.d(TAG, "found ${contours.size} contours")
    val imSize: Double = image!!.total().toDouble()
    //contours.drop(1)
    val each = contours.iterator()
    var idx = 0
    var score = 1e6
    var cwContour = 0

    while (each.hasNext()) {
        val wrapper = each.next()
        val area = Imgproc.contourArea(wrapper)
        val rect = Imgproc.boundingRect(wrapper)
        val aspect = rect.width / rect.height

        if (area > imSize / 16) {
            if (abs(1.0 - aspect) < score) {
                score = abs(1.0 - aspect)
                cwContour = idx
            }
        }
        idx += 1
    }
    return Pair(contours, cwContour)
}

fun cropToCrossword(contour: MatOfPoint, image: Mat, cropSize: Double = 500.0): Mat {
    // finding the minimum rectangle doesn't work since the crossword's edges
    // are curved.

    val imageWarp = Mat()

    image.copyTo(imageWarp)

    val hull = MatOfInt()
    Log.i(TAG, "finding hull")
    Imgproc.convexHull(contour, hull)
    Log.i(TAG, "total hull points ${hull.total()}")
    Log.i(TAG, "hull rows ${hull.rows()}")
    val indexes = hull.toList()

    val contourArray = contour.toArray()
    val listOfPoints = arrayOfNulls<Point>(indexes.size)
    for (i in 0 until indexes.size) {
        listOfPoints[i] = contourArray[(indexes[i])]
    }


    //Imgproc.drawContours(image, hullPointsList, -1, Scalar(255.0, 0.0, 0.0),10);
    Log.i(TAG, "approximating arc length")
    val hullPoints2f = MatOfPoint2f(*listOfPoints.map { it }.toTypedArray())

    val epsilon = 0.05 * Imgproc.arcLength(hullPoints2f, true)

    Log.i(TAG, "approximating poly dp")
    val approxDP = MatOfPoint2f()
    Imgproc.approxPolyDP(hullPoints2f, approxDP, epsilon, true)
    Log.i(TAG, "approxDP size ${approxDP.total()}")

    Log.i(TAG, "approx dp $approxDP")

    val warpCoordinates = MatOfPoint2f(
        Point(cropSize, 0.0),
        Point(cropSize, cropSize),
        Point(0.0, cropSize),
        Point(0.0, 0.0)
    )

    if (approxDP.total() == 4L) {
        Log.i(TAG, "getting perspective transform")
        val warpMat = Imgproc.getPerspectiveTransform(approxDP, warpCoordinates)

        Log.i(TAG, "warping")
        Imgproc.warpPerspective(image, imageWarp, warpMat, image.size())

    }

    Log.i(TAG, "assigning cropped image ")
    val rectToCrop = Rect(0, 0, cropSize.toInt(), cropSize.toInt())
    return imageWarp.submat(rectToCrop)
}

private fun estimateClueBoxSize(croppedToCrosswordImg: Mat): Double {

    Log.i(TAG, "Copying cropped crossword image")
    val image = croppedToCrosswordImg.clone()

    val gradX = Mat()
    val gradY = Mat()
    val edges = Mat()

    Log.i(TAG, "Processing image to find lines")
    Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY)
    // sigmaX = 0.0 defaults the behaviour of this function.
    Imgproc.GaussianBlur(image, image, Size(3.0, 3.0), 0.0)
    Imgproc.Sobel(image, gradX, -1, 2, 0, 3)
    Imgproc.Sobel(image, gradY, -1, 0, 2, 3)

    Log.i(TAG, "summing x and y lines")
    add(gradX, gradY, edges)
    //edges = gradX + gradY

    Log.i(TAG, "thresholding lines image")
    val kernel = Mat.ones(3, 3, CV_32F)
    Imgproc.threshold(edges, edges, 50.0, 255.0, Imgproc.THRESH_BINARY)
    Log.i(TAG, "de-noising with morphology close")
    Imgproc.morphologyEx(edges, edges, Imgproc.MORPH_CLOSE, kernel)

    //edgeImg = edges

    Log.i(TAG, "Finding contours")
    val contours: List<MatOfPoint> = ArrayList()
    val mHierarchy = Mat()
    Imgproc.findContours(
        edges,
        contours,
        mHierarchy,
        Imgproc.RETR_LIST,
        Imgproc.CHAIN_APPROX_SIMPLE
    )
    Log.i(TAG, "found ${contours.size} contours")

    val each = contours.iterator()
    //there is probably a faster way of calculating the median rather than making a list
    // and then
    val sides = mutableListOf<Double>()

    Log.i(TAG, "Iterating through squares to find average size")
    while (each.hasNext()) {
        val contour = each.next()
        if (!Imgproc.isContourConvex(contour)) {
            val contour2f = MatOfPoint2f()
            contour.convertTo(contour2f, CV_32F)
            val rect = Imgproc.minAreaRect(contour2f)
            Log.d(TAG, "rect size: ${rect.size.height}x${rect.size.width}")
            val aspect = rect.size.height / rect.size.width
            if (rect.size.height != 0.0) {
                //sides.add(aspect)
                if (abs(1.0 - aspect) < 0.2) {
                    sides.add(rect.size.width)
                }
            }


        }

    }
//        Log.i(TAG, "calculating the median size")
//        sides.sort()
    val boxSize: Double
    Log.i(TAG, "found ${sides.size} boxes")
    if (sides.size > 0) {
        boxSize = median(sides) //.average()//sides[floor((sides.size).toDouble()/2).toInt()]
        Log.i(TAG, "calculating the median size $boxSize")
    } else {
        boxSize = 10.0 // what should this default to?
    }
    return boxSize

}

private fun getClueBoxMask(croppedToCrosswordImg: Mat): Mat {

    Log.i(TAG, "cloning cropped image for Clue box mask")
    val image = croppedToCrosswordImg.clone()

    Log.i(TAG, "pre processing")

    Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY)
    Imgproc.adaptiveThreshold(
        image, image, 255.0,
        Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 101, 0.0
    )
    val kernel = Mat.ones(3, 3, CV_32F)
    Imgproc.morphologyEx(image, image, Imgproc.MORPH_CLOSE, kernel)

    bitwise_not(image, image)
    Log.i(TAG, "returning binary image of grid")

    return image

}

private fun checkBinaryCrosswordMaskSymmetry(binaryCrosswordImg: Mat): Boolean {
    Log.i(TAG, "Checking for symmetry")
    val matRot = binaryCrosswordImg.clone()
    rotate(binaryCrosswordImg, matRot, ROTATE_180)
    subtract(binaryCrosswordImg, matRot, matRot)
    val isSymmetric = countNonZero(matRot) == 0

    Log.i(TAG, "Crossword symmetric? $isSymmetric")

    return isSymmetric
}

fun makeBinaryCrosswordImg(croppedToCrosswordImg: Mat): Mat? {

    Log.i(TAG, "estimating clue box size")
    val boxSize = estimateClueBoxSize(croppedToCrosswordImg)
    Log.i(TAG, "getting clue box binary image")
    val clueBoxes = getClueBoxMask(croppedToCrosswordImg)

    Log.i(TAG, "calculating resize constants")
    Log.i(TAG, "binary mask has size ${clueBoxes.rows()} x ${clueBoxes.cols()}")
    val rowsD = boxSize.div(clueBoxes.rows())
    val colsD = boxSize.div(clueBoxes.cols())
    Log.i(TAG, "determined these to be $rowsD and $colsD")

    Log.i(TAG, "resizing")
    //Imgproc.resize(clueBoxes,binaryCrosswordImg,Size(), rowsD/2, colsD/2, Imgproc.INTER_LINEAR )
    Imgproc.resize(
        clueBoxes,
        clueBoxes,
        Size(1 / rowsD, 1 / colsD),
        0.0,
        0.0,
        Imgproc.INTER_AREA
    )
    Imgproc.threshold(
        clueBoxes,
        clueBoxes,
        0.0,
        255.0,
        Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU
    )
    Log.i(TAG, "resizing finished")
    Log.d(
        TAG,
        "grid shape ${clueBoxes.rows()}x${clueBoxes.cols()}:\n${clueBoxes.dump()}"
    )

    return if (checkBinaryCrosswordMaskSymmetry(clueBoxes)) {
        clueBoxes
    } else
        null
}

private fun median(list: List<Double>) = list.sorted().let {
    if (it.size % 2 == 0)
        (it[it.size / 2] + it[(it.size - 1) / 2]) / 2
    else
        it[it.size / 2]
}

fun assembleClues(binaryCrosswordImg: Mat): Puzzle {
    val clueMarks = getGridWithClueMarks(binaryCrosswordImg)

    val acrossClues = mutableMapOf<Triple<Int, Int, String>, String>()
    val downClues = mutableMapOf<Triple<Int, Int, String>, String>()

    var clueIdx = 1
    for (col_idx in 0 until clueMarks.cols()) {
        for (row_idx in (clueMarks.rows() - 1) downTo 0) {
            // there are 4 types of boxes in the grid. These are represented by the values
            // 1,2,3,4. 1 is a box which is not the start of a clue. 2 is across. 3 is down
            // 4 is across and down.
            when (clueMarks.get(row_idx, col_idx).toList()[0]) {
                2.0 -> {
                    acrossClues[Triple(clueMarks.rows() - row_idx - 1, col_idx, "")] = "${clueIdx}a"
                    clueIdx += 1
                }

                3.0 -> {
                    downClues[Triple(clueMarks.rows() - row_idx - 1, col_idx, "")] = "${clueIdx}d"
                    clueIdx += 1
                }

                4.0 -> {
                    downClues[Triple(clueMarks.rows() - row_idx - 1, col_idx, "")] = "${clueIdx}d"
                    acrossClues[Triple(clueMarks.rows() - row_idx - 1, col_idx, "")] = "${clueIdx}a"
                    clueIdx += 1
                }
            }
        }
    }

    acrossClues.forEach {
        Log.i(TAG, "${it.key}: ${it.value} ")
    }
    downClues.forEach {
        Log.i(TAG, "${it.key}: ${it.value} ")
    }

    val acrossClueCells = getAcrossClues(binaryCrosswordImg)
    val downClueCells = getDownClues(binaryCrosswordImg)

    val puzzle = Puzzle()

//
    acrossClueCells.forEach { clueCells ->
        val clue = Clue(acrossClues[clueCells[0]]!!, clueCells)
        puzzle.addClue(acrossClues[clueCells[0]]!!, clue)
    }
    downClueCells.forEach { clueCells ->
        val clue = Clue(downClues[clueCells[0]]!!, clueCells)
        puzzle.addClue(downClues[clueCells[0]]!!, clue)
    }

    puzzle.clues.forEach { (name, clue) ->
        Log.d(TAG, "$name: ${clue.clueBoxes}")
    }

    //puzzle.image = gridBitmap
    puzzle.gridSize = binaryCrosswordImg.rows()

    return puzzle

}

private fun getGridWithClueMarks(binaryCrosswordImg: Mat): Mat {
    val binaryGrid = Mat()
    binaryCrosswordImg.convertTo(binaryGrid, CV_8UC1, 1.0 / 255)

    var kernel = Mat.zeros(3, 3, CV_32F)
    kernel.put(1, 0, -1.0)
    kernel.put(1, 2, 1.0)

    val downStarts = convolveGrid(binaryGrid, kernel)

    kernel = Mat.zeros(3, 3, CV_32F)
    kernel.put(0, 1, 1.0)
    kernel.put(2, 1, -1.0)
    val acrossStarts = convolveGrid(binaryGrid, kernel)


    add(binaryGrid, acrossStarts, binaryGrid)
    //add it twice.
    add(binaryGrid, downStarts, binaryGrid)
    add(binaryGrid, downStarts, binaryGrid)

    //binaryGrid.convertTo(binaryCrosswordImg,-1,50.0,0.0)
    return binaryGrid
}

private fun getAcrossClues(binaryCrosswordImg: Mat): List<MutableList<Triple<Int, Int, String>>> {
    var kernel = Mat.zeros(3, 3, CV_32F)
    kernel.put(0, 1, 1.0)
    kernel.put(1, 1, 1.0)

    val anchor = Point(-1.0, -1.0)
    val newGrid = Mat()
    val binaryGrid = Mat()
    binaryCrosswordImg.convertTo(binaryGrid, -1, 1.0 / 250)

    Imgproc.filter2D(binaryGrid, newGrid, -1, kernel, anchor, 0.0, BORDER_CONSTANT)

    kernel = Mat.zeros(3, 3, CV_32F)
    kernel.put(0, 1, -1.0)
    kernel.put(2, 1, 1.0)
    val newGridEnds = Mat()
    Imgproc.filter2D(binaryGrid, newGridEnds, -1, kernel, anchor, 0.0, BORDER_CONSTANT)


    add(newGrid, newGridEnds, newGrid)
    newGrid.convertTo(newGrid, -1, 1.0, -1.0)


    val clueCoordinates = mutableListOf<MutableList<Triple<Int, Int, String>>>()
    var clue = mutableListOf<Triple<Int, Int, String>>()

    //val row = Mat()
    for (col_idx in 0 until newGrid.cols()) {
        //var row = newGrid.row(row_idx)
        for (row_idx in (newGrid.rows() - 1) downTo 0) {
            val cellValue = newGrid.get(row_idx, col_idx).toList()[0]
            //Log.v(TAG,"cell (${row_idx} ${col_idx}) value: ${cellValue}")
            if (cellValue > 0) {
                //  Log.v(TAG,"Adding cell to current Clue")
                clue.add(Triple(newGrid.rows() - row_idx - 1, col_idx, ""))
                //Log.v(TAG,clue.toString())
            } else {
                if (clue.size > 0) {
                    //  Log.v(TAG,"Adding Clue to Clue list and resetting current Clue")
                    clueCoordinates.add(clue)
                    clue = mutableListOf()
                }
            }
        }
        if (clue.size > 0) {
            clueCoordinates.add(clue)
            clue = mutableListOf()
        }
    }

    clueCoordinates.forEach {
        Log.i(TAG, "across clue size ${it.size}")
    }
    // uncomment for debug only!
    // newGrid.convertTo(binaryCrosswordImg,-1,255.0,-1.0)
    return clueCoordinates
}

private fun getDownClues(binaryCrosswordImg: Mat): List<MutableList<Triple<Int, Int, String>>> {
    var kernel = Mat.zeros(3, 3, CV_32F)
    kernel.put(1, 0, 1.0)
    kernel.put(1, 1, 1.0)

    val anchor = Point(-1.0, -1.0)
    val newGrid = Mat()
    val binaryGrid = Mat()
    binaryCrosswordImg.convertTo(binaryGrid, -1, 1.0 / 250)

    Imgproc.filter2D(binaryGrid, newGrid, -1, kernel, anchor, 0.0, BORDER_CONSTANT)

    kernel = Mat.zeros(3, 3, CV_32F)
    kernel.put(1, 0, -1.0)
    kernel.put(1, 2, 1.0)
    val newGridEnds = Mat()
    Imgproc.filter2D(binaryGrid, newGridEnds, -1, kernel, anchor, 0.0, BORDER_CONSTANT)

    add(newGrid, newGridEnds, newGrid)
    newGrid.convertTo(newGrid, -1, 1.0, -1.0)
    //newGrid.convertTo(binaryCrosswordImg,-1,255.0,-1.0)

    //Log.i(TAG,"grid:\n${binaryCrosswordImg.dump()}")

    val clueCoordinates = mutableListOf<MutableList<Triple<Int, Int, String>>>()
    var clue = mutableListOf<Triple<Int, Int, String>>()

    //val row = Mat()
    for (row_idx in (newGrid.rows() - 1) downTo 0) {
        for (col_idx in 0 until newGrid.cols()) {
            val cellValue = newGrid.get(row_idx, col_idx).toList()[0]
            Log.v(TAG, "cell (${row_idx} ${col_idx}) value: $cellValue")
            if (cellValue > 0) {
                Log.v(TAG, "Adding cell to current Clue")
                clue.add(Triple(newGrid.rows() - row_idx - 1, col_idx, ""))
                Log.v(TAG, clue.toString())
            } else {
                if (clue.size > 0) {
                    Log.v(TAG, "Adding Clue to Clue list and resetting current Clue")
                    clueCoordinates.add(clue)
                    clue = mutableListOf()
                }
            }
        }
        if (clue.size > 0) {
            clueCoordinates.add(clue)
            clue = mutableListOf()
        }
    }

    // uncomment for debug only!
    // newGrid.convertTo(binaryCrosswordImg,-1,255.0,-1.0)

    return clueCoordinates
}

private fun convolveGrid(grid: Mat, kernel: Mat): Mat {
    val anchor = Point(-1.0, -1.0)
    val newGrid = Mat()
    Imgproc.filter2D(grid, newGrid, -1, kernel, anchor, 0.0, BORDER_CONSTANT)
    bitwise_and(grid, newGrid, newGrid)
    return newGrid
}


