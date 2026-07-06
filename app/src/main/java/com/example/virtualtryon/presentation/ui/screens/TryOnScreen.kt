package com.example.virtualtryon.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.virtualtryon.R
import com.example.virtualtryon.data.ml.PoseAnalyzer
import com.example.virtualtryon.presentation.viewmodels.TryOnViewModel

@Composable
fun TryOnScreen(
    viewModel: TryOnViewModel,
    poseAnalyzer: PoseAnalyzer
) {
    val context = LocalContext.current
    val currentPose by viewModel.poseState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var userHeightCm by remember { mutableFloatStateOf(170f) }
    var shoulderWidthCm by remember { mutableFloatStateOf(0f) }
    var hipWidthCm by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val hologramVector = ImageVector.vectorResource(id = R.drawable.ic_hologram_shirt)
    val hologramPainter = rememberVectorPainter(image = hologramVector)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            CameraPreviewBox(
                lensFacing = lensFacing,
                onFrame = { imageProxy ->
                    poseAnalyzer.analyze(imageProxy)
                })

            Canvas(modifier = Modifier.fillMaxSize()) {
                val pose = currentPose ?: return@Canvas

                val leftShoulder = pose.leftShoulder
                val rightShoulder = pose.rightShoulder

                if (leftShoulder != null && rightShoulder != null) {
                    val isFront = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val leftX =
                        if (isFront) (1f - leftShoulder.x) * size.width else leftShoulder.x * size.width
                    val rightX =
                        if (isFront) (1f - rightShoulder.x) * size.width else rightShoulder.x * size.width

                    val leftY = leftShoulder.y * size.height
                    val rightY = rightShoulder.y * size.height

                    val shoulderWidth = kotlin.math.abs(rightX - leftX)
                    val centerX = (leftX + rightX) / 2f
                    val centerY = (leftY + rightY) / 2f

                    val garmentWidth = shoulderWidth * 1.6f
                    val garmentHeight = garmentWidth * 1.2f

                    val topLeftX = centerX - (garmentWidth / 2f)
                    val topLeftY = centerY - (garmentHeight * 0.15f)

                    withTransform({
                        translate(left = topLeftX, top = topLeftY)
                        val scaleX = garmentWidth / hologramPainter.intrinsicSize.width
                        val scaleY = garmentHeight / hologramPainter.intrinsicSize.height
                        scale(scaleX, scaleY, Offset.Zero)
                    }) {
                        with(hologramPainter) {
                            draw(hologramPainter.intrinsicSize)
                        }
                    }

                    drawCircle(color = Color.Red, radius = 15f, center = Offset(leftX, leftY))

                    val sleeveWidth = garmentWidth * 0.2f
                    drawRect(
                        color = Color(0x66FF00FF), // Semi-transparent Magenta
                        topLeft = Offset(topLeftX - sleeveWidth, topLeftY),
                        size = androidx.compose.ui.geometry.Size(sleeveWidth, garmentHeight * 0.4f)
                    )
                    drawRect(
                        color = Color(0x66FF00FF),
                        topLeft = Offset(topLeftX + garmentWidth, topLeftY),
                        size = androidx.compose.ui.geometry.Size(sleeveWidth, garmentHeight * 0.4f)
                    )

                    drawCircle(color = Color.Red, radius = 20f, center = Offset(leftX, leftY))
                    drawCircle(color = Color.Red, radius = 20f, center = Offset(rightX, rightY))
                }

                val nose = pose.nose
                val leftHeel = pose.leftHeel
                val rightHeel = pose.rightHeel

                if (nose != null && leftHeel != null && rightHeel != null) {
                    val crownY = nose.y - 0.05f
                    val lowestFootY = maxOf(leftHeel.y, rightHeel.y)

                    val normalizedBodyHeight = lowestFootY - crownY

                    if (normalizedBodyHeight > 0.4f) {
                        val scaleFactor = userHeightCm / normalizedBodyHeight

                        val shoulderDistanceUnits =
                            pose.calculateDistance(leftShoulder, rightShoulder)
                        val hipDistanceUnits = pose.calculateDistance(pose.leftHip, pose.rightHip)

                        shoulderWidthCm = shoulderDistanceUnits * scaleFactor
                        hipWidthCm = hipDistanceUnits * scaleFactor
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                    .background(
                        Color(0x88000000),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Text("Your Height: ${userHeightCm.toInt()} cm", color = Color.White)
                Slider(
                    value = userHeightCm,
                    onValueChange = { userHeightCm = it },
                    valueRange = 140f..210f,
                )

                if (shoulderWidthCm > 0f) {
                    Text(
                        "Shoulder Width: ~${String.format("%.1f", shoulderWidthCm)} cm",
                        color = Color.Cyan
                    )
                    Text("Hip Width: ~${String.format("%.1f", hipWidthCm)} cm", color = Color.Cyan)

                    val estimatedSize = when {
                        shoulderWidthCm < 38f -> "Small (S)"
                        shoulderWidthCm < 44f -> "Medium (M)"
                        shoulderWidthCm < 50f -> "Large (L)"
                        else -> "Extra Large (XL)"
                    }
                    Text(
                        "Recommended Size: $estimatedSize",
                        color = Color.Yellow,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text("Step back so your full body is in frame!", color = Color.Red)
                }
            }

            Button(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                        CameraSelector.LENS_FACING_BACK
                    } else {
                        CameraSelector.LENS_FACING_FRONT
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            ) {
                Text(if (lensFacing == CameraSelector.LENS_FACING_FRONT) "Switch to Back Camera" else "Switch to Front Camera")
            }
        } else {

            Column(modifier = Modifier.align(Alignment.Center)) {
                Text("Camera permission is required for Virtual Try-On", color = Color.White)
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}
