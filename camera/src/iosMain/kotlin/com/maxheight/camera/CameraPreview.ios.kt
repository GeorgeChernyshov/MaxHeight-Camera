package com.maxheight.camera

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIView

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    attachToCamera: (Any) -> Unit,
    onUpdate: (Any) -> Unit
) {
    UIKitView(
        factory = {
            UIView().apply {
                attachToCamera(this)
            }
        },
        modifier = modifier.background(Color.Red),
        update = onUpdate
    )
}