package com.example.learn_opencv

import android.util.Log
import org.opencv.core.*
import org.opencv.core.Core.add
import org.opencv.core.Core.bitwise_not
import org.opencv.core.CvType.CV_32F
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat.zeros
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.floor


class CrosswordDetector {

    val TAG = "CrosswordDetector"

    var unprocessedImg = Mat()

    var croppedToCrosswordImg = Mat(480, 640, CV_8UC3, Scalar(255.0,0.0,255.0));
    var binaryCrosswordImg = Mat(480, 640, CV_8UC3, Scalar(0.0,255.0,255.0));
    var edgeImg = Mat(480, 640, CV_8UC3, Scalar(0.0,255.0,255.0));


    fun process(input_image: Mat?) {
        val (contours,cw_contour) = get_crossword_contour(input_image)
        Imgproc.drawContours(input_image, contours, cw_contour, Scalar(0.0, 255.0, 0.0), 5)

    }

    fun draw_crossword_contour(input_image: Mat?,contours : List<MatOfPoint>?, cw_contour : Int){
        Imgproc.drawContours(input_image, contours, cw_contour, Scalar(0.0, 255.0, 0.0), 5)
    }

    fun get_crossword_contour(input_image: Mat?) : Pair<List<MatOfPoint>, Int> {
        var contours: List<MatOfPoint> = ArrayList()

        //val input_image = rgbaImage
        val image = input_image?.clone()
        val dilation = Mat.ones(5,5,Imgproc.COLOR_BGR2GRAY)
        var mHierarchy = Mat()
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(image,image, Size(7.0,7.0),1.0)
        Imgproc.Canny(image,image,50.0,200.0)
        Imgproc.dilate(image,image, dilation)
        Imgproc.findContours(image,contours,mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
        //Imgproc.drawContours(input_image,contours,-1, Scalar(0.0, 255.0, 0.0), 4)

        Log.d(TAG,"found ${contours.size} countours")
        val im_size : Double = image!!.total().toDouble()
        //contours.drop(1)
        var each = contours.iterator()
        var idx = 0
        var score = 1e6
        var cw_contour = 0

        while (each.hasNext()) {
            val wrapper = each.next()
            val area = Imgproc.contourArea(wrapper)
            val brect = Imgproc.boundingRect(wrapper)
            val aspect = brect.width/brect.height

            if (area > im_size/16) {
                if ( abs(1.0-aspect ) < score) {
                    score = abs(1.0-aspect )
                    cw_contour = idx
                }
            }
            idx = idx + 1
        }
        return Pair(contours,cw_contour)
    }

    fun crop_to_crossword(contour: MatOfPoint, image :Mat)  {
        // finding the minimum rectangle doesnt work since the crossword's edges
        // are curved.
//        val contour2f = MatOfPoint2f()
//        contour.convertTo(contour2f, CV_32F)
//        val rect = Imgproc.minAreaRect(contour2f)
//        val points = arrayOfNulls<Point>(4)
//        rect.points(points)
//        for (i in 0..3) {
//            line(image, points[i], points[(i + 1) % 4], Scalar(255.0, 0.0, 0.0), 10, LINE_AA);
//        }
        val image_warp = Mat()
        image.copyTo(image_warp)

        val hull = MatOfInt()
        Log.i(TAG,"finding hull")
        Imgproc.convexHull(contour,hull)
        Log.i(TAG,"total hull points ${hull.total()}")
        Log.i(TAG,"hull rows ${hull.rows()}")
        val indexes = hull.toList()

        val contour_array = contour.toArray()
        val listOfPoints = arrayOfNulls<Point>(indexes.size)
        for( i in 0..indexes.size - 1){
            listOfPoints[i] = contour_array[(indexes[i])]
        }

        val hullPoints = MatOfPoint(*listOfPoints.map { it }.toTypedArray())
        val hullPointsList = listOf(hullPoints)
        //val hullPointsList_2f = hullPoints_2f.toList()

        //Imgproc.drawContours(image, hullPointsList, -1, Scalar(255.0, 0.0, 0.0),10);
        Log.i(TAG,"approximating arc length")
        val hullPoints_2f = MatOfPoint2f(*listOfPoints.map { it }.toTypedArray())

        val epsilon = 0.05 * Imgproc.arcLength(hullPoints_2f, true)

        Log.i(TAG, "approximating poly dp")
        val approxDP = MatOfPoint2f()
        Imgproc.approxPolyDP(hullPoints_2f, approxDP, epsilon, true)
        Log.i(TAG, "approxDP size ${approxDP.total()}")

        Log.i(TAG,"approx dp $approxDP")

        val warp_coords = MatOfPoint2f(
            Point(500.0, 0.0),
            Point(500.0, 500.0),
            Point(0.0, 500.0),
            Point(0.0, 0.0)
        )

        if (approxDP.total() == 4L) {
            Log.i(TAG, "getthing perspective transform")
            val warp_mat = Imgproc.getPerspectiveTransform(approxDP, warp_coords)

            Log.i(TAG, "warping")
            Imgproc.warpPerspective(image, image_warp, warp_mat, image.size())

            val rectToCrop = Rect(0, 0, 500, 500)
            Log.i(TAG, "assiging cropped image ")
            croppedToCrosswordImg = image_warp.submat(rectToCrop)

        }
        //croppedToCrosswordImg = image_warp

        //return image_warp
    }

    fun estimateClueBoxSize(): Double {

        Log.i(TAG, "Copying cropped crossword image")
        val image = croppedToCrosswordImg?.clone()

        val gradX = Mat()
        val gradY = Mat()
        var edges = Mat()

        Log.i(TAG, "Processing image to find lines")
        Imgproc.cvtColor(image,image,Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(image,image,Size(3.0,3.0), 0.0) //I think sigmax = 0 defaults to an automatic value
        Imgproc.Sobel(image,gradX,-1,2,0,3)
        Imgproc.Sobel(image,gradY,-1,0,2,3)

        Log.i(TAG, "summing x and y lines")
        add(gradX,gradY,edges)
        //edges = gradX + gradY

        Log.i(TAG, "thresholding lines image")
        val kernel = Mat.ones(3,3,CV_32F)
        Imgproc.threshold(edges,edges,50.0, 255.0, Imgproc.THRESH_BINARY)
        Log.i(TAG, "denoising with morphololgyex close")
        Imgproc.morphologyEx(edges,edges,Imgproc.MORPH_CLOSE,kernel)

        edgeImg = edges

        Log.i(TAG, "Finding contours")
        var contours: List<MatOfPoint> = ArrayList()
        var mHierarchy = Mat()
        Imgproc.findContours(edges,contours,mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
        Log.i(TAG, "found ${contours.size} contours")

        var each = contours.iterator()
        var idx = 0
        //there is probably a faster way of calculating the median rather than making a list
        // and then
        val sides = mutableListOf<Double>()

        Log.i(TAG, "Iterating through squares to find average size")
        while (each.hasNext()) {
            val contour = each.next()
            if( !Imgproc.isContourConvex(contour) ){
                val contour2f = MatOfPoint2f()
                contour.convertTo(contour2f, CV_32F)
                val rect = Imgproc.minAreaRect(contour2f)
                Log.d(TAG, "rect size: ${rect.size.height}x${rect.size.width}")
                val aspect = rect.size.height/rect.size.width
                if( rect.size.height != 0.0){
                    //sides.add(aspect)
                    if( abs(1.0-aspect) < 0.2){
                        sides.add(rect.size.width)
                    }
                }


            }

        }
//        Log.i(TAG, "calculating the median size")
//        sides.sort()
        Log.i(TAG, "found ${sides.size} boxes")
        val boxSize = sides.average()//sides[floor((sides.size).toDouble()/2).toInt()]
        Log.i(TAG, "calculating the median size $boxSize")
        return boxSize


    }

    private fun getClueBoxMask(): Mat? {

        Log.i(TAG, "cloning cropped image for clue box mask")
        val image = croppedToCrosswordImg?.clone()

        Log.i(TAG, "pre processing")

        Imgproc.cvtColor(image,image,Imgproc.COLOR_BGR2GRAY)
        Imgproc.adaptiveThreshold(image,image,255.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,101,0.0)
        val kernel = Mat.ones(9,9,CV_32F)
        Imgproc.morphologyEx(image,image,Imgproc.MORPH_CLOSE,kernel)

        bitwise_not(image,image)
        Log.i(TAG, "returing binary image of grid")

        return image

    }

    fun makeBinaryCrosswordImg() {

        Log.i(TAG, "estimatating clue box size")
        val boxSize = estimateClueBoxSize()
        Log.i(TAG, "getting clue box binary image")
        val clueBoxes = getClueBoxMask()

        Log.i(TAG, "calculating resize constants")
        Log.i(TAG, "binary mask has size ${clueBoxes!!.rows()} x ${clueBoxes!!.cols()}")
        val rowsD = clueBoxes!!.rows().div(boxSize)
        val colsD = clueBoxes!!.cols().div(boxSize)
        Log.i(TAG, "determined these to be $rowsD and $colsD")

        //Log.i(TAG, "resizing")
        //Imgproc.resize(clueBoxes,binaryCrosswordImg,Size(),1/rowsD,1/colsD, Imgproc.INTER_LINEAR )
        //Log.i(TAG, "resizing finished")

    }

}