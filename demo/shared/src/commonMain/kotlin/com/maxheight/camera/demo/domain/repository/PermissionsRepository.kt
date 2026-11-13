package com.maxheight.camera.demo.domain.repository

import com.maxheight.camera.demo.domain.model.PermissionsEvent
import com.maxheight.camera.demo.domain.model.PermissionsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class PermissionsRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    val permissionsState: StateFlow<PermissionsState>
        field = MutableStateFlow(PermissionsState())

    val events: SharedFlow<PermissionsEvent>
        field = MutableSharedFlow<PermissionsEvent>()

    protected abstract suspend fun requestCameraPermissionInternal() : Boolean
    protected abstract suspend fun requestRecordAudioPermissionInternal() : Boolean

    // Convenience functions
    val hasCameraPermission get() = permissionsState
        .value
        .hasCameraPermission

    val hasRecordAudioPermission get() = permissionsState
        .value
        .hasRecordAudioPermission

    private var isPermissionRequestPending: Boolean = false

    fun requestCameraPermission() = scope.launch {
        if (isPermissionRequestPending)
            return@launch

        isPermissionRequestPending = true
        val isGranted = requestCameraPermissionInternal()
        permissionsState.value = permissionsState.value.copy(
            hasCameraPermission = isGranted
        )

        if (isGranted)
            events.emit(PermissionsEvent.CameraPermissionGranted)

        isPermissionRequestPending = false
    }

    fun requestRecordAudioPermission() = scope.launch {
        if (isPermissionRequestPending)
            return@launch

        isPermissionRequestPending = true
        val isGranted = requestRecordAudioPermissionInternal()
        permissionsState.value = permissionsState.value.copy(
            hasRecordAudioPermission = isGranted
        )

        if (isGranted)
            events.emit(PermissionsEvent.AudioRecordingPermissionGranted)

        isPermissionRequestPending = false
    }
}