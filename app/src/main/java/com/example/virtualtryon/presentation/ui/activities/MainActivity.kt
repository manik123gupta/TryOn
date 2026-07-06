package com.example.virtualtryon.presentation.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.virtualtryon.data.ml.PoseAnalyzer
import com.example.virtualtryon.presentation.ui.theme.VirtualTryOnTheme
import com.example.virtualtryon.presentation.ui.screens.TryOnScreen
import com.example.virtualtryon.presentation.viewmodels.TryOnViewModel
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker

class MainActivity : ComponentActivity() {

    private val viewModel: TryOnViewModel by viewModels()
    private lateinit var poseAnalyzer: PoseAnalyzer
    private lateinit var poseLandmarker: PoseLandmarker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_lite.task")
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, _ ->
                if (::poseAnalyzer.isInitialized) {
                    poseAnalyzer.handleResult(result)
                }
            }
            .setErrorListener { error ->
                Log.e("MainActivity", "MediaPipe Error: ", error)
            }
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(this, options)
        poseAnalyzer = PoseAnalyzer(poseLandmarker) { pose ->
            viewModel.updatePose(pose)
        }

        enableEdgeToEdge()
        setContent {
            VirtualTryOnTheme {
                TryOnScreen(
                    viewModel,
                    poseAnalyzer
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::poseLandmarker.isInitialized) {
            poseLandmarker.close()
        }
    }
}
