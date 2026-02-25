package com.maxheight.camera

import com.maxheight.camera.internal.CaptureFileOutputDelegate
import com.maxheight.camera.internal.VideoDataOutputSampleBufferDelegate
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.Foundation.NSURL
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.darwin.dispatch_queue_create
import platform.darwin.dispatch_queue_t

@OptIn(ExperimentalForeignApi::class)
actual class Camera {

    private val _state = MutableStateFlow(CameraState())
    actual val state = _state.asStateFlow()

    private var captureSession: AVCaptureSession? = null
    private var previewLayer: AVCaptureVideoPreviewLayer? = null
    private var currentUiView: UIView? = null
    private var movieFileOutput: AVCaptureMovieFileOutput? = null
    private var videoDataOutput: AVCaptureVideoDataOutput? = null
    private var videoDataOutputQueue: dispatch_queue_t? = null
    private var sampleBufferDelegate = VideoDataOutputSampleBufferDelegate()
    private val captureFileOutputDelegate = CaptureFileOutputDelegate()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    actual fun start(
        cameraType: CameraType,
        onBeforeStart: () -> Unit,
        onAfterStart: () -> Unit,
        onError: (Throwable) -> Unit,
        onProcessFrame: ((Any) -> Unit)?
    ) {
        if (captureSession?.isRunning() == true) {
            println("CameraHandler: captureSession is already running.")
            return
        }

        val session = AVCaptureSession().apply {
            sessionPreset = AVCaptureSessionPresetHigh
        }

        captureSession = session

        try {
            val videoDevice = when (cameraType) {
                CameraType.BACK -> AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
                CameraType.FRONT -> {
                    AVCaptureDevice.defaultDeviceWithDeviceType(
                        deviceType = AVCaptureDeviceTypeBuiltInWideAngleCamera,
                        mediaType = AVMediaTypeVideo,
                        position = AVCaptureDevicePositionFront
                    )
                }

                CameraType.ULTRA_WIDE_LENS -> {
                    val deviceDiscoverySession = AVCaptureDeviceDiscoverySession
                        .discoverySessionWithDeviceTypes(
                            deviceTypes = listOf(
                                AVCaptureDeviceTypeBuiltInUltraWideCamera
                            ),
                            mediaType = AVMediaTypeVideo,
                            position = AVCaptureDevicePositionBack
                        )

                    deviceDiscoverySession.devices.firstOrNull() as? AVCaptureDevice
                        ?: AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
                }
            }

            val videoInput = videoDevice?.let {
                inputFromDevice(it)
            }

            if (videoInput != null && session.canAddInput(videoInput)) {
                session.addInput(videoInput)
            } else {
                return
            }

            currentUiView?.let {
                attachPreview(containerView = it)
            }

            onProcessFrame?.let {
                sampleBufferDelegate.processFrame = it
                videoDataOutput = AVCaptureVideoDataOutput().apply {
                    alwaysDiscardsLateVideoFrames = true
                }

                videoDataOutputQueue = dispatch_queue_create("VideoDataOutputQueue", null)
                videoDataOutput!!.setSampleBufferDelegate(
                    sampleBufferDelegate,
                    videoDataOutputQueue
                )

                if (session.canAddOutput(videoDataOutput!!)) {
                    session.addOutput(videoDataOutput!!)
                }
            }

            onBeforeStart()
            scope.launch {
                session.startRunning()
            }

            _state.value = _state.value.copy(cameraStarted = true)
            onAfterStart()
        } catch (e: Exception) {
            stop()
            onError(e)
        }
    }

    actual fun stop(
        onBeforeStop: () -> Unit,
        onAfterStop: () -> Unit
    ) {
        onBeforeStop()
        if (captureSession?.isRunning() == true)
            captureSession?.stopRunning()

        previewLayer?.removeFromSuperlayer()
        captureSession = null
        previewLayer = null
        movieFileOutput = null
        videoDataOutput?.setSampleBufferDelegate(null, null)
        videoDataOutput = null
        videoDataOutputQueue = null

        _state.value = _state.value.copy(cameraStarted = false)
        onAfterStop()
    }

    actual fun startRecording(
        file: Any,
        onRecordingFinalized: (String?, String?) -> Unit,
        onRecordingStarted: () -> Unit
    ) {
        if (file !is NSURL) return

        val output = AVCaptureMovieFileOutput()
        movieFileOutput = output

        captureSession?.beginConfiguration()

        if (captureSession?.canAddOutput(output) == true) {
            captureSession?.addOutput(output)
            captureSession?.commitConfiguration()
        } else {
            println("CameraHandler: Failed to add movie file output to session.")
        }

        captureFileOutputDelegate.onRecordingStarted = onRecordingStarted
        captureFileOutputDelegate.onRecordingFinalized = onRecordingFinalized

        output.startRecordingToOutputFileURL(
            outputFileURL = file,
            recordingDelegate = captureFileOutputDelegate
        )

        _state.value = _state.value.copy(isRecording = true)
    }

    actual fun stopRecording(onRecordingStopped: () -> Unit) {
        if (movieFileOutput?.isRecording() == true)
            movieFileOutput?.stopRecording()

        _state.value = _state.value.copy(isRecording = false)
        onRecordingStopped()
    }

    actual fun pauseRecording(onRecordingPaused: () -> Unit) {
        movieFileOutput?.pauseRecording()
        _state.value = _state.value.copy(isRecordingPaused = true)
        onRecordingPaused()
    }

    actual fun resumeRecording(onRecordingResumed: () -> Unit) {
        movieFileOutput?.resumeRecording()
        _state.value = _state.value.copy(isRecordingPaused = false)
        onRecordingResumed()
    }

    actual fun attachPreview(preview: Any) {
        if (preview !is UIView)
            return

        currentUiView = preview
        attachPreview(containerView = preview)
    }

    actual fun removePreview() {
        previewLayer?.removeFromSuperlayer()
        previewLayer?.finalize()
        previewLayer = null
    }

    actual fun onPreviewUpdate(view: Any) {
        if (view !is UIView)
            return

        previewLayer?.frame = view.bounds
    }

    private fun attachPreview(containerView: UIView) = scope.launch(Dispatchers.Main) {
        val session = captureSession ?: return@launch

        if (previewLayer?.session == session) {
            previewLayer?.frame = containerView.bounds
            return@launch
        }

        previewLayer = AVCaptureVideoPreviewLayer(
            session = session
        ).apply {
            frame = containerView.bounds
            videoGravity = AVLayerVideoGravityResizeAspectFill
        }

        containerView.layer.addSublayer(previewLayer!!)
        containerView.autoresizingMask = UIViewAutoresizingFlexibleWidth or
                UIViewAutoresizingFlexibleHeight

        previewLayer?.frame = containerView.bounds
    }

    private fun inputFromDevice(
        device: AVCaptureDevice
    ): AVCaptureDeviceInput? {
        return try {
            AVCaptureDeviceInput.deviceInputWithDevice(
                device = device,
                error = null
            )
        } catch (e: Exception) {
            null
        }
    }
}