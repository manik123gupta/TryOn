# TryOn

Virtual Try-On Android App 👕✨

A real-time, augmented reality Virtual Try-On Android application built with Jetpack Compose, CameraX, and Google MediaPipe.

This application uses on-device machine learning to track full-body poses in real-time, dynamically overlay garments (like a holographic t-shirt) onto the user's body, and estimate real-world clothing sizes based on user height calibration.

🚀 Key Features

Real-Time Body Tracking: Utilizes MediaPipe Pose Landmarker to track 33 3D body landmarks at high FPS.

Dynamic Garment Overlay: Renders a responsive 2D holographic garment that scales, translates, and mirrors perfectly based on shoulder width and position.

AI Sizing & Measurement Engine: Calculates real-world measurements (in cm) for shoulders and hips by establishing a pixel-to-centimeter scale factor based on the user's inputted height. Recommends clothing sizes (S, M, L, XL).

Front & Back Camera Integration: Seamlessly switches between lenses with conditional X-axis mirroring to maintain accurate AR tracking.

Anti-Jitter Smoothing: Implements an Exponential Moving Average (EMA) mathematical filter in the ViewModel to eliminate ML micro-fluctuations, ensuring a buttery-smooth garment overlay.

🛠 Tech Stack & Architecture

Built with modern Android development standards focusing on scalability and performance:

UI: Jetpack Compose, Material 3

Camera: CameraX (Preview, ImageAnalysis)

Machine Learning: MediaPipe Tasks Vision (Pose Detection)

Concurrency: Kotlin Coroutines & StateFlow

Architecture: MVVM (Model-View-ViewModel) paired with Clean Architecture principles.

Data Layer: PoseAnalyzer extracts hardware frames and runs background C++ ML inference.

Domain Layer: BodyPose & PoseLandmark abstract the ML SDK away from the app logic.

Presentation Layer: TryOnViewModel handles state and smoothing math; Jetpack Compose renders the UI.

⚙️ Getting Started

Prerequisites

Android Studio (Koala or newer recommended)

Minimum SDK: 24

Target SDK: 37

Kotlin 2.2.10+ (using the new bundled Compose compiler)

⚠️ Important Setup Step: Download the ML Model

Because the machine learning model is too large for version control, you must download it manually before running the app.

Download the Pose Landmarker (Lite) model from the Official MediaPipe Documentation.

Rename the downloaded file to pose_landmarker_lite.task.

In Android Studio, navigate to app/src/main/.

Create a new directory named assets (if it doesn't exist).

Move the pose_landmarker_lite.task file into the app/src/main/assets/ directory.

Build and Run

Clone this repository.

Sync the Gradle files to download the CameraX and MediaPipe dependencies.

Build and deploy to a physical Android device (Emulators may have poor camera frame rates).

Grant Camera permissions when prompted.

📐 How the Sizing Engine Works

MediaPipe returns coordinates in a normalized space (0.0 to 1.0). To calculate physical sizes:

The user inputs their real-world height (e.g., 170 cm).

The app measures the normalized distance from the crown of the head to the lowest heel.

A Scale Factor (cm per 1.0 unit) is calculated: UserHeightCm / NormalizedBodyHeight.

The Euclidean distance between the left and right shoulder landmarks is multiplied by this scale factor to estimate real-world shoulder width.