package com.maxheight.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraPreview(
    modifier: Modifier,
    attachToCamera: (Any) -> Unit,
    onUpdate: (Any) -> Unit
)