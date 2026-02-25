package com.maxheight.camera.demo.data

interface PermissionResultEmitter {
    fun requestPermission(
        permission: String,
        onPermissionResult: (Boolean) -> Unit
    )
}