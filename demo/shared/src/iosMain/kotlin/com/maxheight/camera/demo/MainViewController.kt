package com.maxheight.camera.demo

import androidx.compose.ui.window.ComposeUIViewController
import com.maxheight.camera.Camera
import com.maxheight.camera.demo.data.LocalStorageRepositoryImpl
import com.maxheight.camera.demo.data.PermissionsRepositoryImpl
import com.maxheight.camera.demo.ui.App

fun MainViewController() = ComposeUIViewController {
    App(
        camera = Camera(),
        permissionsRepository = PermissionsRepositoryImpl(),
        localStorageRepository = LocalStorageRepositoryImpl()
    )
}