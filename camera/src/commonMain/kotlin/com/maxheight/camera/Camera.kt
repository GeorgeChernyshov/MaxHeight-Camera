package com.maxheight.camera

import kotlinx.coroutines.flow.StateFlow

expect class Camera {
    val state: StateFlow<CameraState>

    fun start(
        cameraType: CameraType = CameraType.BACK,
        onBeforeStart: () -> Unit = {},
        onAfterStart: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
        onProcessFrame: ((Any) -> Unit)? = null
    )

    fun stop(
        onBeforeStop: () -> Unit = {},
        onAfterStop: () -> Unit = {}
    )

    fun startRecording(
        file: Any,
        onRecordingFinalized: (String?, String?) -> Unit,
        onRecordingStarted: () -> Unit = {},
    )

    fun stopRecording(
        onRecordingStopped: () -> Unit = {}
    )

    fun pauseRecording(
        onRecordingPaused: () -> Unit = {}
    )

    fun resumeRecording(
        onRecordingResumed: () -> Unit = {}
    )

    fun attachPreview(preview: Any)
    fun removePreview()
    fun onPreviewUpdate(view: Any)
}