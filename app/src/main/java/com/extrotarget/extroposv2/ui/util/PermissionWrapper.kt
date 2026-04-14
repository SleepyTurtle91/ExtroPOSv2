package com.extrotarget.extroposv2.ui.util

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

enum class PermissionState { INITIAL, GRANTED, DENIED }

@Composable
fun CameraPermissionWrapper(
    onPermissionGranted: @Composable () -> Unit,
    onPermissionDenied: @Composable (Boolean) -> Unit
) {
    val context = LocalContext.current
    var permissionState by remember {
        mutableStateOf(
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                PermissionState.GRANTED
            } else {
                PermissionState.INITIAL
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissionState = if (granted) PermissionState.GRANTED else PermissionState.DENIED
        }
    )

    LaunchedEffect(key1 = true) {
        if (permissionState == PermissionState.INITIAL) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    when (permissionState) {
        PermissionState.GRANTED -> onPermissionGranted()
        PermissionState.DENIED -> onPermissionDenied(false)
        PermissionState.INITIAL -> {} 
    }
}

@Composable
fun BluetoothPermissionWrapper(
    onPermissionGranted: @Composable () -> Unit,
    onPermissionDenied: @Composable (Boolean) -> Unit
) {
    val context = LocalContext.current
    val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        Manifest.permission.BLUETOOTH_CONNECT
    } else {
        Manifest.permission.BLUETOOTH
    }

    var permissionState by remember {
        mutableStateOf(
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                PermissionState.GRANTED
            } else {
                PermissionState.INITIAL
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissionState = if (granted) PermissionState.GRANTED else PermissionState.DENIED
        }
    )

    LaunchedEffect(key1 = true) {
        if (permissionState == PermissionState.INITIAL) {
            launcher.launch(permission)
        }
    }

    when (permissionState) {
        PermissionState.GRANTED -> onPermissionGranted()
        PermissionState.DENIED -> onPermissionDenied(false)
        PermissionState.INITIAL -> {}
    }
}
