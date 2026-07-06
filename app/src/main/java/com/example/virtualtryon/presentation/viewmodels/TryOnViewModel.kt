package com.example.virtualtryon.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.virtualtryon.domain.models.BodyPose
import com.example.virtualtryon.domain.models.PoseLandmark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TryOnViewModel : ViewModel() {

    private val _poseState = MutableStateFlow<BodyPose?>(null)
    val poseState = _poseState.asStateFlow()

    private var smoothedLeft: PoseLandmark? = null
    private var smoothedRight: PoseLandmark? = null

    private val alpha = 0.2f

    fun updatePose(newPose: BodyPose) {
        val rawLeft = newPose.leftShoulder
        val rawRight = newPose.rightShoulder

        val areShouldersVisible = rawLeft != null && rawLeft.visibility > 0.5f &&
                rawRight != null && rawRight.visibility > 0.5f

        if (areShouldersVisible && rawLeft != null && rawRight != null) {
            smoothedLeft = applyEma(rawLeft, smoothedLeft)
            smoothedRight = applyEma(rawRight, smoothedRight)

            val newLandmarks = newPose.landmarks.toMutableList()
            newLandmarks[11] = smoothedLeft!!
            newLandmarks[12] = smoothedRight!!

            _poseState.update { BodyPose(newLandmarks) }
        }
    }

    private fun applyEma(newVal: PoseLandmark, oldVal: PoseLandmark?): PoseLandmark {
        if (oldVal == null) return newVal
        return PoseLandmark(
            x = (newVal.x * alpha) + (oldVal.x * (1 - alpha)),
            y = (newVal.y * alpha) + (oldVal.y * (1 - alpha)),
            z = (newVal.z * alpha) + (oldVal.z * (1 - alpha)),
            visibility = newVal.visibility
        )
    }
}
