package com.dubtechno.generator.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.dubtechno.generator.ui.theme.DubTechnoTheme
import com.dubtechno.generator.viewmodel.MainViewModel

/**
 * Main Activity for the Dub Techno Generator
 * Handles permissions and hosts the main UI
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, proceed
        } else {
            // Permission denied, show message
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request audio permissions if needed
        checkAudioPermissions()

        setContent {
            DubTechnoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val snackbarHostState = remember { SnackbarHostState() }

                    // Show snackbar messages from ViewModel
                    LaunchedEffect(viewModel.message) {
                        viewModel.message.collect { message ->
                            message?.let {
                                snackbarHostState.showSnackbar(it)
                                viewModel.clearMessage()
                            }
                        }
                    }

                    MainScreen(viewModel = viewModel, snackbarHostState = snackbarHostState)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup is handled in ViewModel.onCleared()
    }

    private fun checkAudioPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Show explanation to user
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}
