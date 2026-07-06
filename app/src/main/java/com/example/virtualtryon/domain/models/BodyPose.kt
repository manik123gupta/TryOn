package com.example.virtualtryon.domain.models

data class BodyPose(
    val landmarks: List<PoseLandmark>
) {
    val nose get() = landmarks.getOrNull(0)
    val leftShoulder get() = landmarks.getOrNull(11)
    val rightShoulder get() = landmarks.getOrNull(12)
    val leftHip get() = landmarks.getOrNull(23)
    val rightHip get() = landmarks.getOrNull(24)
    val leftHeel get() = landmarks.getOrNull(29)
    val rightHeel get() = landmarks.getOrNull(30)

    fun calculateDistance(p1: PoseLandmark?, p2: PoseLandmark?): Float {
        if (p1 == null || p2 == null) return 0f
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
