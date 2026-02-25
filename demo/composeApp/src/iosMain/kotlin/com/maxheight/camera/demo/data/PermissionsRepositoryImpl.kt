package com.maxheight.camera.demo.data

import com.maxheight.camera.demo.domain.repository.PermissionsRepository
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.requestAccessForMediaType
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PermissionsRepositoryImpl : PermissionsRepository() {

    override suspend fun requestCameraPermissionInternal() : Boolean =
        suspendCoroutine { continuation ->
            AVCaptureDevice.requestAccessForMediaType(
                AVMediaTypeVideo,
                continuation::resume
            )
        }

    override suspend fun requestRecordAudioPermissionInternal() : Boolean =
        suspendCoroutine { continuation ->
        AVCaptureDevice.requestAccessForMediaType(
            AVMediaTypeAudio,
            continuation::resume
        )
    }
}