package com.dubtechno.generator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DubTechnoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AudioControlScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioEngine.stop()
    }
}

@Composable
fun AudioControlScreen() {
    var isPlaying by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf(440f) }
    var volume by remember { mutableStateOf(50f) }
    var sampleRate by remember { mutableStateOf(0) }
    var bufferSize by remember { mutableStateOf(0) }
    var latency by remember { mutableStateOf(0.0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Dub Techno Generator",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FF88)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Start/Stop Button
        Button(
            onClick = {
                if (isPlaying) {
                    AudioEngine.stop()
                    isPlaying = false
                } else {
                    val started = AudioEngine.start()
                    if (started) {
                        isPlaying = true
                        // Update audio info
                        sampleRate = AudioEngine.getSampleRate()
                        bufferSize = AudioEngine.getBufferSize()
                        latency = AudioEngine.getLatencyMillis()
                        // Set initial parameters
                        AudioEngine.setFrequency(frequency)
                        AudioEngine.setVolume(volume / 100f)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) Color(0xFFFF4444) else Color(0xFF00FF88),
                contentColor = Color.Black
            )
        ) {
            Text(
                text = if (isPlaying) "STOP" else "START",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status indicator
        StatusCard(
            title = "Status",
            value = if (isPlaying) "PLAYING" else "STOPPED",
            color = if (isPlaying) Color(0xFF00FF88) else Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Frequency Slider
        ControlSlider(
            label = "Frequency",
            value = frequency,
            valueRange = 200f..800f,
            unit = "Hz",
            enabled = isPlaying,
            onValueChange = { newFreq ->
                frequency = newFreq
                if (isPlaying) {
                    AudioEngine.setFrequency(newFreq)
                }
            }
        )

        // Volume Slider
        ControlSlider(
            label = "Volume",
            value = volume,
            valueRange = 0f..100f,
            unit = "%",
            enabled = isPlaying,
            onValueChange = { newVolume ->
                volume = newVolume
                if (isPlaying) {
                    AudioEngine.setVolume(newVolume / 100f)
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Audio Info
        if (isPlaying) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow("Sample Rate", "$sampleRate Hz")
                InfoRow("Buffer Size", "$bufferSize frames")
                InfoRow("Latency", "%.2f ms".format(latency))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatusCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFF888888)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ControlSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    color = if (enabled) Color(0xFF00FF88) else Color(0xFF666666)
                )
                Text(
                    text = "${value.roundToInt()} $unit",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.White else Color(0xFF666666)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00FF88),
                    activeTrackColor = Color(0xFF00FF88),
                    inactiveTrackColor = Color(0xFF333333),
                    disabledThumbColor = Color(0xFF666666),
                    disabledActiveTrackColor = Color(0xFF333333),
                    disabledInactiveTrackColor = Color(0xFF222222)
                )
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF888888)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF00FF88)
        )
    }
}

@Composable
fun DubTechnoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF00FF88),
            onPrimary = Color.Black,
            background = Color(0xFF0A0A0A),
            surface = Color(0xFF1A1A1A),
            onSurface = Color.White
        ),
        content = content
    )
}
