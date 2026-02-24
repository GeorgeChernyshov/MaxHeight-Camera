package com.maxheight.camera.demo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxheight.camera.Camera
import com.maxheight.camera.CameraPreview
import com.maxheight.camera.demo.domain.model.PermissionsEvent
import com.maxheight.camera.demo.domain.repository.LocalStorageRepository
import com.maxheight.camera.demo.domain.repository.PermissionsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import maxheight_camera.demo.composeapp.generated.resources.Res
import maxheight_camera.demo.composeapp.generated.resources.camera_active
import maxheight_camera.demo.composeapp.generated.resources.ic_pause_24
import maxheight_camera.demo.composeapp.generated.resources.ic_play_24
import maxheight_camera.demo.composeapp.generated.resources.ic_stop_24
import maxheight_camera.demo.composeapp.generated.resources.pause_recording
import maxheight_camera.demo.composeapp.generated.resources.recording_active
import maxheight_camera.demo.composeapp.generated.resources.recording_paused
import maxheight_camera.demo.composeapp.generated.resources.resume_recording
import maxheight_camera.demo.composeapp.generated.resources.start_recording
import maxheight_camera.demo.composeapp.generated.resources.stop_recording
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
@Preview
fun App(
    camera: Camera,
    permissionsRepository: PermissionsRepository,
    localStorageRepository: LocalStorageRepository
) {
    val cameraState = camera.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var eventsCollector by remember { mutableStateOf<Job?>(null) }
    var isShowingPreview by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        eventsCollector = coroutineScope.launch {
            permissionsRepository.events.collect { event ->
                when (event) {
                    PermissionsEvent.CameraPermissionGranted -> {
                        camera.start()
                        isShowingPreview = true
                    }

                    PermissionsEvent.AudioRecordingPermissionGranted -> {
                        camera.startRecording(
                            file = localStorageRepository.createTempFile(),
                            onRecordingFinalized = { path, _ ->
                                path?.let {
                                    localStorageRepository.deleteLocalFile(it)
                                }
                            }
                        )
                    }
                }
            }
        }

        if (permissionsRepository.hasCameraPermission) {
            camera.start()
        } else {
            permissionsRepository.requestCameraPermission()
        }

        onDispose {
            camera.removePreview()
            camera.stop()
            eventsCollector?.cancel()
            eventsCollector = null
        }
    }

    MaterialTheme {
        Box(Modifier.fillMaxSize()
            .safeDrawingPadding()
        ) {
            if (isShowingPreview) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    attachToCamera = camera::attachPreview,
                    onUpdate = camera::onPreviewUpdate
                )

                Text(
                    text = stringResource(
                        resource = when {
                            cameraState.value.isRecordingPaused -> Res.string.recording_paused
                            cameraState.value.isRecording -> Res.string.recording_active
                            else -> Res.string.camera_active
                        }
                    ),
                    modifier = Modifier.align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )

                RecorderControls(
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    isRecording = cameraState.value.isRecording,
                    isRecordingPaused = cameraState.value.isRecordingPaused,
                    onStartRecordingClick = {
                        if (permissionsRepository.hasRecordAudioPermission) {
                            camera.startRecording(
                                file = localStorageRepository.createTempFile(),
                                onRecordingFinalized = { path, _ ->
                                    path?.let {
                                        localStorageRepository.deleteLocalFile(it)
                                    }
                                }
                            )
                        } else permissionsRepository.requestRecordAudioPermission()
                    },
                    onPauseRecordingClick = camera::pauseRecording,
                    onResumeRecordingClick = camera::resumeRecording,
                    onStopRecordingClick = camera::stopRecording
                )
            }
        }
    }
}

@Composable
fun RecorderControls(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    isRecordingPaused: Boolean,
    onStartRecordingClick: () -> Unit,
    onPauseRecordingClick: () -> Unit,
    onResumeRecordingClick: () -> Unit,
    onStopRecordingClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isRecording || isRecordingPaused) {
            Button(onClick = {
                if (isRecording)
                    onResumeRecordingClick()
                else onStartRecordingClick()
            }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_play_24),
                    contentDescription = stringResource(
                        resource = if (isRecording)
                            Res.string.resume_recording
                        else Res.string.start_recording
                    )
                )
            }
        }

        if (isRecording && !isRecordingPaused) {
            Button(onClick = onPauseRecordingClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_pause_24),
                    contentDescription = stringResource(Res.string.pause_recording)
                )
            }
        }

        if (isRecording) {
            Button(onClick = onStopRecordingClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_stop_24),
                    contentDescription = stringResource(Res.string.stop_recording)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecorderControlsPreview() {
    MaterialTheme {
        RecorderControls(
            isRecording = true,
            isRecordingPaused = false,
            onStartRecordingClick = {},
            onPauseRecordingClick = {},
            onResumeRecordingClick = {},
            onStopRecordingClick = {}
        )
    }
}