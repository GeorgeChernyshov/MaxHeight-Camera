package com.maxheight.camera

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CameraStateTest {

    @Test
    fun testDefaultState() {
        val state = CameraState()
        assertFalse(state.cameraStarted, "Camera should not be started by default")
        assertFalse(state.isRecording, "Camera should not be recording by default")
        assertFalse(state.isRecordingPaused, "Recording should not be paused by default")
    }

    @Test
    fun testStateTransitions() {
        var state = CameraState()

        // 1. Initial -> Started
        state = state.copy(cameraStarted = true)
        assertTrue(state.cameraStarted, "cameraStarted should be true")
        assertFalse(state.isRecording, "isRecording should remain false")

        // 2. Started -> Recording
        state = state.copy(isRecording = true)
        assertTrue(state.cameraStarted, "cameraStarted should remain true")
        assertTrue(state.isRecording, "isRecording should be true")
        assertFalse(state.isRecordingPaused, "isRecordingPaused should remain false")

        // 3. Recording -> Paused
        state = state.copy(isRecordingPaused = true)
        assertTrue(state.isRecording, "isRecording should remain true")
        assertTrue(state.isRecordingPaused, "isRecordingPaused should be true")

        // 4. Paused -> Resumed (Not Paused)
        state = state.copy(isRecordingPaused = false)
        assertTrue(state.isRecording, "isRecording should remain true")
        assertFalse(state.isRecordingPaused, "isRecordingPaused should be false")

        // 5. Recording -> Stopped
        state = state.copy(isRecording = false)
        assertFalse(state.isRecording, "isRecording should be false")
        assertFalse(state.isRecordingPaused, "isRecordingPaused should be false")

        // 6. Camera Stop
        state = state.copy(cameraStarted = false)
        assertFalse(state.cameraStarted, "cameraStarted should be false")
    }
}