package com.example.learn_opencv

import android.util.Log
import com.google.android.material.color.utilities.Score.score
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

class CrosswordDetector {

    val TAG = "CrosswordDetector"

    fun process(input_image: Mat?) {
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

        Log.i(TAG,"found ${contours.size} countours")
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

        Imgproc.drawContours(input_image,contours,cw_contour, Scalar(0.0, 255.0, 0.0), 5)

    }
}