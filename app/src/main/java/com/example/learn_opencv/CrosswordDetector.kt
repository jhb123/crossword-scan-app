package com.example.learn_opencv

import android.util.Log
import org.opencv.core.*
import org.opencv.core.CvType.CV_32F
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.LINE_AA
import org.opencv.imgproc.Imgproc.line
import java.lang.Long
import kotlin.math.abs


class CrosswordDetector {

    val TAG = "CrosswordDetector"

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

    fun crop_to_crossword(contour: MatOfPoint, image :Mat) : Mat {
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

        Imgproc.drawContours(image, hullPointsList, -1, Scalar(255.0, 0.0, 0.0),10);
        Log.i(TAG,"approximating arc length")
        val hullPoints_2f = MatOfPoint2f(*listOfPoints.map { it }.toTypedArray())

        val epsilon = 0.05 * Imgproc.arcLength(hullPoints_2f, true)

        Log.i(TAG, "approximating poly dp")
        val approxDP = MatOfPoint2f()
        Imgproc.approxPolyDP(hullPoints_2f, approxDP, epsilon, true)
        Log.i(TAG, "approxDP size ${approxDP.total()}")

        Log.i(TAG,"approx dp $approxDP")

        val warp_coords = MatOfPoint2f(
            Point(500.0, 10.0),
            Point(500.0, 500.0),
            Point(10.0, 500.0),
            Point(10.0, 10.0)
        )

        if (approxDP.total() == 4L) {
            Log.i(TAG, "getthing perspective transform")
            val warp_mat = Imgproc.getPerspectiveTransform(approxDP, warp_coords)

            Log.i(TAG, "warping")
            Imgproc.warpPerspective(image, image_warp, warp_mat, image.size())
        }

        return image_warp
    }

}