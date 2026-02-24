package com.maxheight.camera.demo.data

import android.Manifest
import com.maxheight.camera.demo.domain.repository.PermissionsRepository
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PermissionsRepositoryImpl : PermissionsRepository() {

    private var permissionResultEmitter = WeakReference<PermissionResultEmitter>(null)

    override suspend fun requestCameraPermissionInternal(): Boolean =
        suspendCoroutine { continuation ->
            permissionResultEmitter.get()?.requestPermission(
                permission = Manifest.permission.CAMERA,
                onPermissionResult = continuation::resume
            )
        }

    override suspend fun requestRecordAudioPermissionInternal(): Boolean =
        suspendCoroutine { continuation ->
            permissionResultEmitter.get()?.requestPermission(
                permission = Manifest.permission.RECORD_AUDIO,
                onPermissionResult = continuation::resume
            )
        }

    fun setPermissionResultEmitter(emitter: PermissionResultEmitter?) {
        this.permissionResultEmitter = WeakReference(emitter)
    }
}