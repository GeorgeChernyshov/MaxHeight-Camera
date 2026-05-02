package com.maxheight.camera

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CameraDeviceTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    @Test
    fun testCameraInitialization() {
        runBlocking {
            ActivityScenario.launch(TestActivity::class.java).use { scenario ->
                var camera: Camera? = null
                scenario.onActivity { activity ->
                    camera = Camera(activity).apply {
                        setLifecycleOwner(activity)
                        start(
                            onError = { throw it }
                        )
                    }
                }

                withTimeout(10000) {
                    val state = camera!!.state.first { it.cameraStarted }
                    assertTrue(state.cameraStarted, "Camera should be started")
                }

                scenario.onActivity {
                    camera?.stop()
                }
            }
        }
    }

    @Test
    fun testPreviewAttachment() {
        runBlocking {
            ActivityScenario.launch(TestActivity::class.java).use { scenario ->
                var camera: Camera? = null
                scenario.onActivity { activity ->
                    camera = Camera(activity).apply {
                        setLifecycleOwner(activity)
                        val previewView = PreviewView(activity)
                        attachPreview(previewView)
                        start(
                            onError = { throw it }
                        )
                    }
                }

                withTimeout(10000) {
                    val state = camera!!.state.first { it.cameraStarted }
                    assertTrue(state.cameraStarted, "Camera should be started with preview attached")
                }

                scenario.onActivity {
                    camera?.stop()
                }
            }
        }
    }
}