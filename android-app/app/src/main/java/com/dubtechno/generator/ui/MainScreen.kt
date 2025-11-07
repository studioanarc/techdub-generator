package com.dubtechno.generator.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dubtechno.generator.ui.theme.GlassOverlay
import com.dubtechno.generator.ui.theme.Primary
import com.dubtechno.generator.viewmodel.MainViewModel

/**
 * Main screen with tabs and transport controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    snackbarHostState: SnackbarHostState
) {
    var selectedTab by remember { mutableStateOf(0) }
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isInitialized by viewModel.isInitialized.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Dub Techno Generator")
                        if (!isInitialized) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { /* Info */ }) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            TransportControls(
                viewModel = viewModel,
                isPlaying = isPlaying,
                isInitialized = isInitialized
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Generative") },
                    icon = { Icon(Icons.Default.Build, null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Synths") },
                    icon = { Icon(Icons.Default.Create, null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Effects") },
                    icon = { Icon(Icons.Default.Star, null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Presets") },
                    icon = { Icon(Icons.Default.List, null) }
                )
            }

            // Content
            when (selectedTab) {
                0 -> GenerativeControlsScreen(viewModel)
                1 -> SynthControlsScreen(viewModel)
                2 -> EffectsScreen(viewModel)
                3 -> PresetsScreen(viewModel)
            }
        }
    }
}

/**
 * Transport controls at the bottom
 */
@Composable
fun TransportControls(
    viewModel: MainViewModel,
    isPlaying: Boolean,
    isInitialized: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Visualizer
            val waveformData by viewModel.waveformData.collectAsState()
            WaveformVisualizer(
                waveformData = waveformData,
                isPlaying = isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 16.dp)
            )

            // Transport buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Randomize button
                FilledTonalButton(
                    onClick = { viewModel.randomize() },
                    enabled = isInitialized,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Randomize",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play/Stop button (large)
                PlayStopButton(
                    isPlaying = isPlaying,
                    isInitialized = isInitialized,
                    onClick = {
                        if (isPlaying) viewModel.stop() else viewModel.play()
                    }
                )

                // Volume control
                var showVolumeSlider by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showVolumeSlider = !showVolumeSlider },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Email, // Using Email as volume placeholder
                        contentDescription = "Volume",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * Large play/stop button with animation
 */
@Composable
fun PlayStopButton(
    isPlaying: Boolean,
    isInitialized: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    FilledIconButton(
        onClick = onClick,
        enabled = isInitialized,
        modifier = Modifier
            .size((96 * if (isPlaying) scale else 1f).dp)
            .clip(CircleShape)
            .background(
                brush = if (isPlaying) {
                    Brush.radialGradient(
                        colors = listOf(Primary, Primary.copy(alpha = 0.6f))
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            ),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.Transparent
        )
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Stop" else "Play",
            modifier = Modifier.size(48.dp),
            tint = if (isPlaying) Color.Black else Color.White
        )
    }
}

/**
 * Waveform visualizer
 */
@Composable
fun WaveformVisualizer(
    waveformData: FloatArray,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2

        if (!isPlaying || waveformData.isEmpty()) {
            // Draw flat line when not playing
            drawLine(
                color = Primary.copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(0f, midY),
                end = androidx.compose.ui.geometry.Offset(width, midY),
                strokeWidth = 2f
            )
            return@Canvas
        }

        // Draw waveform
        val path = androidx.compose.ui.graphics.Path()
        val stepWidth = width / waveformData.size

        waveformData.forEachIndexed { index, sample ->
            val x = index * stepWidth
            val y = midY + (sample * midY * 0.8f)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = Primary,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )

        // Draw glow effect
        drawPath(
            path = path,
            color = Primary.copy(alpha = 0.3f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
        )
    }
}
