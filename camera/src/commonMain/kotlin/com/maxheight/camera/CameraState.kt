package com.maxheight.camera

data class CameraState(
    val cameraStarted: Boolean = false,
    val isRecording: Boolean = false,
    val isRecordingPaused: Boolean = false
)