package com.maxheight.camera.demo.domain.model

sealed class PermissionsEvent {
    object CameraPermissionGranted : PermissionsEvent()
    object AudioRecordingPermissionGranted : PermissionsEvent()
}