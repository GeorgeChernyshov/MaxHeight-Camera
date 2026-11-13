# MaxHeight Camera

A Kotlin Multiplatform camera library for Android and iOS, with a shared camera controller and a Compose Multiplatform preview. Android uses CameraX; iOS uses AVFoundation.

Use it to display a live preview, select a camera, and record video from shared Kotlin code. Camera construction, permissions, and output files stay platform-specific.

## Platform support

| Platform       | Targets                       | Backend                                                    |
|----------------|-------------------------------|------------------------------------------------------------|
| Android        | `androidTarget`, API 24+      | CameraX                                                    |
| iOS devices    | `iosArm64`                    | AVFoundation                                               |
| iOS simulators | `iosSimulatorArm64`, `iosX64` | AVFoundation; capture depends on available camera hardware |

There are no desktop or web targets. Use a physical iOS device to test capture. The demo app targets iOS 18.2; this is the demo's deployment setting, not a declared library minimum.

## Installation

Add Google Maven and Maven Central to your repositories:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

Add the dependency to your shared module:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.georgechernyshov:camera:1.0.4")
        }
    }
}
```

Your shared UI module needs Compose Multiplatform and the Compose compiler plugin. The examples below also use Compose Foundation and Runtime. This repository builds with Kotlin 2.3.0 and Compose Multiplatform 1.10.0; dependency versions are in [the version catalog](gradle/libs.versions.toml).

## Platform setup

The library does not request permissions. Obtain camera permission before calling `start()` and handle denial in your app.

### Android

Declare camera permission in the app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

Request it at runtime, for example with `ActivityResultContracts.RequestPermission`. Create the camera with a `Context` and set a `LifecycleOwner` before starting it:

```kotlin
// androidMain, inside a ComponentActivity named MainActivity
val camera = Camera(this).apply {
    setLifecycleOwner(this@MainActivity)
}
```

Import `com.maxheight.camera.Camera`. Keep this instance for the lifetime of the screen instead of recreating it on recomposition. See the demo's [MainActivity](demo/shared/src/androidMain/kotlin/com/maxheight/camera/demo/MainActivity.kt) for permission and lifecycle wiring.

### iOS

Add a camera usage description to the host app's `Info.plist`:

```xml
<key>NSCameraUsageDescription</key>
<string>Allow camera access to preview and record video.</string>
```

Request video access through AVFoundation before starting the camera. Create and retain an instance in `iosMain`, then pass it to your shared UI:

```kotlin
import com.maxheight.camera.Camera

val camera = Camera()
```

The demo's [iOS permission repository](demo/shared/src/iosMain/kotlin/com/maxheight/camera/demo/data/PermissionsRepositoryImpl.kt) shows how to check and request authorization.

## Show a preview

This shared composable assumes camera permission has already been granted and the platform setup above is complete:

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.maxheight.camera.Camera
import com.maxheight.camera.CameraPreview
import com.maxheight.camera.CameraType

@Composable
fun CameraScreen(camera: Camera, onError: (Throwable) -> Unit) {
    CameraPreview(
        modifier = Modifier.fillMaxSize(),
        attachToCamera = camera::attachPreview,
        onUpdate = camera::onPreviewUpdate,
    )

    DisposableEffect(camera) {
        camera.start(
            cameraType = CameraType.BACK,
            onError = onError,
        )
        onDispose {
            camera.stopRecording()
            camera.removePreview()
            camera.stop()
        }
    }
}
```

`CameraPreview` creates an Android `PreviewView` or an iOS `UIView` and attaches it to the controller. Pass `onPreviewUpdate` so the iOS preview layer follows changes to the view's bounds.

Choose `CameraType.BACK` (the default), `FRONT`, or `ULTRA_WIDE_LENS`. Ultra-wide selection falls back to the default back camera when a suitable device is unavailable. On Android it selects the shortest focal length among exposed back cameras when more than one is available.

For a camera change, stop any recording, call `stop()`, then call `start(cameraType = ...)`. On iOS, calling `start()` while the session is running returns without changing the camera.

## Record video

Create the destination in platform code and pass it to shared code as `Any`:

| Platform | Required argument to `startRecording(file = ...)`             |
|----------|---------------------------------------------------------------|
| Android  | `java.io.File` in a writable directory                        |
| iOS      | `platform.Foundation.NSURL` pointing to a writable local file |

For example, on Android:

```kotlin
val outputFile = java.io.File(context.filesDir, "capture-${System.currentTimeMillis()}.mp4")
```

On iOS, using an existing writable app directory:

```kotlin
val outputFile = platform.Foundation.NSURL.fileURLWithPath(
    "$documentsDirectory/capture.mov"
)
```

Use a fresh destination for each recording. The demo includes platform-specific [Android](demo/shared/src/androidMain/kotlin/com/maxheight/camera/demo/data/LocalStorageRepositoryImpl.kt) and [iOS](demo/shared/src/iosMain/kotlin/com/maxheight/camera/demo/data/LocalStorageRepositoryImpl.kt) file helpers.

Once the camera is running, shared code can control recording:

```kotlin
fun recordVideo(camera: Camera, outputFile: Any) {
    camera.startRecording(
        file = outputFile,
        onRecordingStarted = {
            println("Recording started")
        },
        onRecordingFinalized = { path, error ->
            if (error != null) {
                println("Recording failed: $error")
            } else if (path != null) {
                println("Video saved to $path")
            }
        },
    )
}

// Invoke these from your recording controls:
// camera.pauseRecording()
// camera.resumeRecording()
// camera.stopRecording()
```

`onRecordingFinalized` receives a local path on success or an error message on failure. Wait for this callback before reading, sharing, or deleting the output: `stopRecording()` and its callback do not mean the file has finished writing. The app owns storage and any export to the photo library or shared media storage.

Current recording is video-only: Android does not enable audio on the recorder, and iOS does not add an audio input. The demo requests microphone permission, but granting it does not enable audio capture.

## Observe state

`camera.state` is a `StateFlow<CameraState>` with three flags, all initially `false`:

| Property            | Meaning                                       |
|---------------------|-----------------------------------------------|
| `cameraStarted`     | The controller has issued a start request     |
| `isRecording`       | The controller has issued a recording request |
| `isRecordingPaused` | The controller has issued a pause request     |

In Compose:

```kotlin
val state by camera.state.collectAsState()
```

Import `androidx.compose.runtime.collectAsState` and `androidx.compose.runtime.getValue` for the snippet above.

These flags reflect controller calls rather than confirmed hardware readiness. `onAfterStart` can run before native startup finishes; on Android, `onRecordingStarted` also runs before the native recording-start event. Callbacks are not consistently dispatched to the UI thread across platforms, so dispatch UI work to the main thread as needed.

## Current limitations

- **Startup errors:** `onError` does not cover every failure path. For example, iOS can return without invoking it when no usable input is available. Do not treat `cameraStarted` as proof of a working preview.
- **Recording state:** stopping the camera does not reset recording flags, and stopping a recording does not clear the paused flag. Stop recording explicitly before stopping the camera; resume a paused recording before stopping if your UI depends on that flag.
- **Frame processing:** `start(onProcessFrame = ...)` exposes native frames as `Any`. iOS forwards `CMSampleBufferRef` values on a dedicated queue. Android creates an `ImageAnalysis` use case but does not bind it, so frame callbacks currently do not run there.
- **Native types:** preview and file arguments use `Any`, you have to specify types manually
- **Capture controls:** there is no shared API for still photos, zoom, flash, focus, audio capture, or configurable video quality. Android requests CameraX's highest recording quality; iOS uses the high session preset.

## Demo and development

From the repository root, build the library and demo:

```sh
./gradlew :camera:assembleDebug :demo:composeApp:assembleDebug
```

Run the Android demo through the `demo/shared` run configuration in Android Studio. On macOS, open [`demo/iosApp/iosApp.xcodeproj`](demo/iosApp/iosApp.xcodeproj) in Xcode, configure signing for your device, and run the `iosApp` scheme. Ensure the host has the camera usage description described above.

Run the checks used by CI:

```sh
./gradlew :camera:testDebugUnitTest :camera:lintRelease :camera:detekt

# Requires a connected Android device or running emulator:
./gradlew :camera:connectedDebugAndroidTest

# macOS with Xcode and an available iOS simulator:
./gradlew :camera:iosSimulatorArm64Binaries :camera:iosSimulatorArm64Test
```

On Windows, replace `./gradlew` with `.\gradlew.bat`. Common tests cover the state model; Android instrumentation tests exercise initialization and preview attachment. Recording and camera hardware behavior need device testing.

The demo deletes successful recordings in its finalization callback. Change that callback if you want to keep or inspect the videos.
