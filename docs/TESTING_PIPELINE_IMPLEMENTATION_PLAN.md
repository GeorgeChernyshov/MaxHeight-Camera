# Testing Pipeline Implementation Plan - MaxHeight Camera

This document outlines the strategy for implementing instrumented tests on real devices for both Android and iOS, ensuring mirrored validation of camera functionality.

## 1. Objectives
*   **Hardware Validation**: Verify camera initialization, preview, and recording on physical devices.
*   **Platform Parity**: Ensure that test cases for Android (`androidTest`) are mirrored in iOS (`iosTest`).
*   **Local-First**: Achieve successful test execution on local hardware before integrating into CI/CD.

## 2. Instrumented Testing Strategy

### Android: `androidTest` (Instrumented)
We will use the existing Android testing infrastructure to interact with the CameraX API on-device.
*   **Framework**: `androidx.test.ext.junit` and `Espresso` (for UI-bound checks).
*   **Focus**: 
    *   Verify `Camera.start()` binds use cases correctly in a real `Lifecycle`.
    *   Confirm `PreviewView` receives a stream (surface provider connection).
    *   Validate file creation during `startRecording()`.

### iOS: `iosTest` (XCTest Integration)
We will implement tests in `iosTest` that call into the `Camera` actual implementation.
*   **Framework**: Kotlin Native `kotlin.test` running on iOS Simulators/Devices.
*   **Focus**:
    *   Verify `AVCaptureSession` state transitions to `isRunning`.
    *   Confirm `AVCaptureVideoPreviewLayer` is correctly attached to the view hierarchy.
    *   Validate `AVCaptureMovieFileOutput` begins writing to the provided `NSURL`.

## 3. Proposed Test Suite

| Test Class             | Platform | Type         | Purpose                                                                                                        |
|:-----------------------|:---------|:-------------|:---------------------------------------------------------------------------------------------------------------|
| `CameraStateTest`      | Common   | Unit         | Validates that `CameraState` correctly reflects updates to `cameraStarted`, `isRecording`, etc.                |
| `CameraDeviceTest`     | Android  | Instrumented | Verifies `Camera.start()` correctly binds `Preview` and `VideoCapture` use cases to a real `Lifecycle`.        |
| `CameraDeviceTest`     | iOS      | Instrumented | Verifies `AVCaptureSession` initialization and state transitions to `isRunning`.                               |
| `CameraRecordingTest`  | Android  | Instrumented | Validates `startRecording` creates a physical file and triggers the `onRecordingFinalized` callback correctly. |
| `CameraRecordingTest`  | iOS      | Instrumented | Validates `AVCaptureMovieFileOutput` writes to `NSURL` and handles recording lifecycle callbacks.              |
| `CameraPreviewUiTest`  | Both     | UI (Compose) | Asserts that the `CameraPreview` composable correctly instantiates the native preview (PreviewView/UIView).    |
| `CameraLensSwitchTest` | Both     | Instrumented | Specifically verifies camera switching logic (Back vs Front vs Ultra-wide) on hardware that supports it.       |

## 4. Implementation Steps

### Step 1: Establish Test Infrastructure
*   **Common**: Create `src/commonTest/kotlin/com/maxheight/camera/CameraStateTest.kt` for shared logic.
*   **Android**: Create a `TestActivity` in `androidMain` (debug) or `androidTest` to provide a `LifecycleOwner` and `Context`.
*   **Instrumented Files**: Create the following files in both `src/androidTest/kotlin/...` and `src/iosTest/kotlin/...`:
    *   `CameraDeviceTest.kt`
    *   `CameraRecordingTest.kt`
    *   `CameraLensSwitchTest.kt`
    *   `CameraPreviewUiTest.kt`

### Step 2: Mirrored Test Cases
For every core feature, we will maintain an identical test suite across platforms:

1.  **State Management (`CameraStateTest`)**:
    *   `testDefaultState`: Asserts initial values.
    *   `testStateTransitions`: Asserts flow updates when camera/recording starts.
2.  **Hardware Initialization (`CameraDeviceTest`)**:
    *   `testCameraInitialization`: Checks if the session starts without errors (CameraX vs AVFoundation).
    *   `testPreviewAttachment`: Checks if the native preview view is correctly linked to the session.
3.  **Recording Lifecycle (`CameraRecordingTest`)**:
    *   `testRecordingFlow`: `start` -> `pause` -> `resume` -> `stop` and verify physical file creation.
4.  **UI Integration (`CameraPreviewUiTest`)**:
    *   `testPreviewPlacement`: Asserts that the `CameraPreview` composable correctly instantiates and displays the native view.
5.  **Lens Selection (`CameraLensSwitchTest`)**:
    *   `testLensRotation`: Verifies switching logic for `BACK`, `FRONT`, and `ULTRA_WIDE_LENS`.

### Step 3: Local Execution on Real Devices
*   **Android**: Run via `./gradlew :camera:connectedDebugAndroidTest`.
*   **iOS**: Run via `./gradlew :camera:iosSimulatorArm64Test` (or target a connected device).

## 5. CI/CD Pipeline Strategy

### Android (Feasible)
*   **Option A**: GitHub Actions with an Android Emulator (supports hardware-accelerated camera simulation).
*   **Option B**: Firebase Test Lab (Real devices) triggered via CLI.

### iOS (Challenging)
*   **Simulator**: Basic session checks can run on GitHub Actions macOS runners, but hardware-specific features (like Ultra Wide lens) will fail.
*   **Real Devices**: Requires a dedicated Mac mini or services like AWS Device Farm / Bitrise.

## 6. Next Actions (Immediate)
- [ ] Create a `TestActivity` in `androidMain` (debug only) or `androidTest` to host the camera.
- [ ] Implement `CameraStateTest` in `commonTest`.
- [ ] Implement `CameraDeviceTest` (Android & iOS).
- [ ] Implement `CameraRecordingTest` (Android & iOS).
- [ ] Implement `CameraPreviewUiTest` (Android & iOS).
- [ ] Implement `CameraLensSwitchTest` (Android & iOS).
