package com.jhb.crosswordScan

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.MutableState
import androidx.core.content.FileProvider
import androidx.core.graphics.rotationMatrix
import androidx.exifinterface.media.ExifInterface
import java.io.File

private const val TAG = "photolauncher"

class PhotoLauncher(val componentActivity: ComponentActivity) {

    private var latestTmpUri: Uri? = null

//    private var _bitmap : MutableState<Bitmap?> = mutableStateOf(null)
//    var bitmap : State<Bitmap?> = _bitmap
    lateinit var bitmap : MutableState<Bitmap?> //= mutableStateOf(null)

    fun takeImage() {

        getTmpFileUri().let { uri ->
            Log.i(TAG,"tmp uri $uri")
            latestTmpUri = uri
            Log.i(TAG,"launching photo capture")
            takeImageResult.launch(uri)
            Log.i(TAG,"setting bitmap photo")
            //imageSetter(bitmap)
        }


    }

    private val takeImageResult =
        componentActivity.registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                val contentResolver: ContentResolver = componentActivity.contentResolver

                latestTmpUri?.let { uri ->
                    var rotationMatrix = rotationMatrix(0f, 0f, 0f)
                    val exif = contentResolver.openInputStream(uri)?.let { ExifInterface(it) }
                    if (exif != null) {
                        Log.i(TAG, "input image rotation ${exif.rotationDegrees.toFloat()}")
                        rotationMatrix = rotationMatrix(exif.rotationDegrees.toFloat(), 0f, 0f)
                    }
                    val bitmap_test = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    val rotatedBitmap = Bitmap.createBitmap(
                        bitmap_test,
                        0,
                        0,
                        bitmap_test.width,
                        bitmap_test.height,
                        rotationMatrix,
                        true
                    )
                    bitmap.value = rotatedBitmap
                }

            }
        }

    private fun getTmpFileUri(): Uri {
        val cacheDir = componentActivity.getCacheDir()
        val tmpFile = File.createTempFile("tmp_image_file", ".bmp", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(componentActivity, componentActivity.packageName +  ".provider", tmpFile)
    }
}