package com.example.virtualtryon.data.ml

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.virtualtryon.domain.models.BodyPose
import com.example.virtualtryon.domain.models.PoseLandmark
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseAnalyzer(
    private val poseLandmarker: PoseLandmarker,
    private val onPoseDetected: (BodyPose) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        val frameTimeMs = imageProxy.imageInfo.timestamp

        var bitmap = imageProxy.toBitmap()
        if (bitmap.width > bitmap.height) {
            val matrix = Matrix()
            matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        val mpImage = BitmapImageBuilder(bitmap).build()
        try {
            poseLandmarker.detectAsync(mpImage, frameTimeMs)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }

    fun handleResult(result: PoseLandmarkerResult) {
        val firstPose = result.landmarks().firstOrNull() ?: return

        val domainLandmarks = firstPose.map { landmark ->
            PoseLandmark(
                x = landmark.x(),
                y = landmark.y(),
                z = landmark.z(),
                visibility = landmark.visibility().orElse(0f)
            )
        }

        onPoseDetected(BodyPose(domainLandmarks))
    }
}
