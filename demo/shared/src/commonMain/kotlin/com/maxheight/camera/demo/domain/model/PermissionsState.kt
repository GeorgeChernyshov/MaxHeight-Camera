package com.maxheight.camera.demo.domain.model

data class PermissionsState(
    val hasCameraPermission: Boolean = false,
    val hasRecordAudioPermission: Boolean = false
)