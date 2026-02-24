package com.maxheight.camera.demo

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.maxheight.camera.Camera
import com.maxheight.camera.demo.data.LocalStorageRepositoryImpl
import com.maxheight.camera.demo.data.PermissionResultEmitter
import com.maxheight.camera.demo.data.PermissionsRepositoryImpl
import com.maxheight.camera.demo.ui.App

class MainActivity : ComponentActivity(), PermissionResultEmitter {

    private var onPermissionResult: ((Boolean) -> Unit) = {}
    private var permissionResultLauncher = registerForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { onPermissionResult(it) }

    private val camera = Camera(this)
    private val permissionsRepository = PermissionsRepositoryImpl()
    private val localStorageRepository = LocalStorageRepositoryImpl(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        camera.setLifecycleOwner(this)

        setContent {
            App(
                camera = camera,
                permissionsRepository = permissionsRepository,
                localStorageRepository = localStorageRepository
            )
        }
    }

    override fun onStart() {
        super.onStart()

        permissionsRepository.setPermissionResultEmitter(this)
    }

    override fun onStop() {
        permissionsRepository.setPermissionResultEmitter(null)

        super.onStop()
    }

    override fun requestPermission(
        permission: String,
        onPermissionResult: (Boolean) -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                this, permission
            ) == PERMISSION_GRANTED) {
            onPermissionResult(true)
            return
        }

        this.onPermissionResult = onPermissionResult
        permissionResultLauncher.launch(permission)
    }
}