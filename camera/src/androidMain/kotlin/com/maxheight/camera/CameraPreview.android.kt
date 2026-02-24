package com.maxheight.camera

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    attachToCamera: (Any) -> Unit,
    onUpdate: (Any) -> Unit
) {
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                attachToCamera(this)
            }
        },
        modifier = modifier,
        update = onUpdate
    )
}