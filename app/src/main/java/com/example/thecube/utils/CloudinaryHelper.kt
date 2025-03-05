package com.example.thecube.utils

import android.graphics.Bitmap
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.ByteArrayOutputStream

object CloudinaryHelper {

    /**
     * Uploads a Bitmap image to Cloudinary.
     *
     * @param bitmap The Bitmap to upload.
     * @param onSuccess Callback invoked with the secure URL when the upload is successful.
     * @param onError Callback invoked with the error description if the upload fails.
     */
    fun uploadBitmap(bitmap: Bitmap, onSuccess: (String?) -> Unit, onError: (String?) -> Unit) {
        // Convert Bitmap to ByteArray
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val byteArray = baos.toByteArray()

        MediaManager.get().upload(byteArray)
            .option("folder", "images-the-cube")  // Organize uploads in a folder
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("CloudinaryUpload", "Upload started")
                }
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    val progress = (bytes.toDouble() / totalBytes.toDouble()) * 100
                    Log.d("CloudinaryUpload", "Progress: ${"%.2f".format(progress)}%")
                }
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val secureUrl = resultData?.get("secure_url") as? String
                    Log.d("CloudinaryUpload", "Upload successful: $secureUrl")
                    onSuccess(secureUrl)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("CloudinaryUpload", "Upload error: ${error?.description}")
                    onError(error?.description)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.w("CloudinaryUpload", "Upload rescheduled: ${error?.description}")
                    onError(error?.description)
                }
            })
            .dispatch()
    }
}
