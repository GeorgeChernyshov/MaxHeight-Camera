package com.maxheight.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

actual class Camera(private val context: Context) {

    private val _state = MutableStateFlow(CameraState())
    actual val state = _state.asStateFlow()

    private var videoCaptureInternal: VideoCapture<Recorder>? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var currentPreviewView: PreviewView? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var recording: Recording? = null
    private var imageAnalysisUseCase: ImageAnalysis? = null
    private var imageAnalysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    actual fun start(
        cameraType: CameraType,
        onBeforeStart: () -> Unit,
        onAfterStart: () -> Unit,
        onError: (Throwable) -> Unit,
        onProcessFrame: ((Any) -> Unit)?
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Preview
            previewUseCase = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(currentPreviewView?.surfaceProvider)
                }

            if (onProcessFrame != null) {
                imageAnalysisUseCase = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Keep only latest frame for real-time analysis
                    .build()
                    .also {
                        it.setAnalyzer(imageAnalysisExecutor) { imageProxy ->
                            onProcessFrame(imageProxy)
                        }
                    }
            }

            // VideoCapture
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()

            videoCaptureInternal = VideoCapture.withOutput(recorder)

            val cameraSelector = when (cameraType) {
                CameraType.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
                CameraType.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
                CameraType.ULTRA_WIDE_LENS -> selectUltraWideCamera(cameraProvider!!)
            }

            onBeforeStart()

            try {
                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()
                // Bind use cases to camera
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner!!,
                    cameraSelector,
                    previewUseCase,
                    videoCaptureInternal
                )

            } catch (ex: Exception) {
                onError(ex)
            }
        }, ContextCompat.getMainExecutor(context))

        _state.value = _state.value.copy(cameraStarted = true)
        onAfterStart()
    }

    actual fun stop(
        onBeforeStop: () -> Unit,
        onAfterStop: () -> Unit
    ) {
        onBeforeStop()
        cameraProvider?.unbindAll()
        cameraProvider = null
        previewUseCase = null
        videoCaptureInternal = null
        imageAnalysisUseCase?.clearAnalyzer()
        imageAnalysisUseCase = null
        imageAnalysisExecutor.shutdown()
        imageAnalysisExecutor = Executors.newSingleThreadExecutor()

        _state.value = _state.value.copy(cameraStarted = false)
        onAfterStop()
    }

    actual fun startRecording(
        file: Any,
        onRecordingFinalized: (String?, String?) -> Unit,
        onRecordingStarted: () -> Unit
    ) {
        if (file !is File) return

        if (videoCaptureInternal == null) {
            Log.e("CameraHandler", "Video capture not initialized.")
            onRecordingFinalized(null, "Video capture not initialized.")
            return
        }

        val outputOptions = FileOutputOptions.Builder(file).build()

        recording = videoCaptureInternal?.output
            ?.prepareRecording(context, outputOptions)
            ?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Log.i("CameraHandler", "Recording Started")
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            Log.i("CameraHandler", "Video capture succeeded: ${file.absolutePath}")
                            onRecordingFinalized(file.absolutePath, null)
                        } else {
                            recording?.close()
                            recording = null
                            val errorMessage = "Video capture failed: ${recordEvent.error}"
                            Log.e("CameraHandler", errorMessage)
                            onRecordingFinalized(null, errorMessage)
                        }
                    }
                }
            }

        _state.value = _state.value.copy(isRecording = true)
        onRecordingStarted()
    }

    actual fun stopRecording(onRecordingStopped: () -> Unit) {
        recording?.stop()
        recording = null
        _state.value = _state.value.copy(isRecording = false)
        onRecordingStopped()
    }

    actual fun pauseRecording(onRecordingPaused: () -> Unit) {
        recording?.pause()
        _state.value = _state.value.copy(isRecordingPaused = true)
        onRecordingPaused()
    }

    actual fun resumeRecording(onRecordingResumed: () -> Unit) {
        recording?.resume()
        _state.value = _state.value.copy(isRecordingPaused = false)
        onRecordingResumed()
    }

    actual fun attachPreview(preview: Any) {
        if (preview !is PreviewView)
            return

        previewUseCase?.setSurfaceProvider(preview.surfaceProvider)
        currentPreviewView = preview
    }

    actual fun removePreview() {
        previewUseCase?.setSurfaceProvider(null)
        currentPreviewView = null
    }

    actual fun onPreviewUpdate(view: Any) {}

    fun setLifecycleOwner(owner: LifecycleOwner) {
        lifecycleOwner = owner
    }

    @OptIn(ExperimentalCamera2Interop::class)
    private fun selectUltraWideCamera(
        cameraProvider: ProcessCameraProvider
    ): CameraSelector {
        val backCameraInfos = cameraProvider.availableCameraInfos.filter {
            it.lensFacing == CameraSelector.LENS_FACING_BACK
        }

        val ultraWideInfo = backCameraInfos.minByOrNull { cameraInfo ->
            val characteristics = Camera2CameraInfo.from(cameraInfo)
            val focalLengths = characteristics.getCameraCharacteristic(
                CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
            )

            focalLengths?.minOrNull() ?: Float.MAX_VALUE
        }

        if (ultraWideInfo != null && backCameraInfos.size > 1) {
            val id = Camera2CameraInfo.from(ultraWideInfo).cameraId

            return CameraSelector.Builder()
                .addCameraFilter { cameraInfos ->
                    cameraInfos.filter { Camera2CameraInfo.from(it).cameraId == id }
                }
                .build()
        }

        return CameraSelector.DEFAULT_BACK_CAMERA
    }
}