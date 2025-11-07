package com.dubtechno.generator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dubtechno.generator.data.ScaleType
import com.dubtechno.generator.ui.components.LabeledSlider
import com.dubtechno.generator.ui.components.NoteSelector
import com.dubtechno.generator.ui.components.ScaleSelector
import com.dubtechno.generator.viewmodel.MainViewModel

/**
 * Generative controls screen with simple and advanced modes
 */
@Composable
fun GenerativeControlsScreen(viewModel: MainViewModel) {
    val config by viewModel.config.collectAsState()
    var showAdvanced by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Simple controls card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Basic Controls",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Root Note Selector
                Text("Root Note", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                NoteSelector(
                    selectedNote = config.rootNote,
                    onNoteSelected = { viewModel.setRootNote(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scale Selector
                Text("Scale", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                ScaleSelector(
                    selectedScale = config.scale,
                    onScaleSelected = { viewModel.setScale(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tempo Slider
                LabeledSlider(
                    label = "Tempo",
                    value = config.tempo,
                    valueRange = 100f..140f,
                    onValueChange = { viewModel.setTempo(it) },
                    valueDisplay = "${config.tempo.toInt()} BPM"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Density Slider
                LabeledSlider(
                    label = "Density",
                    value = config.density,
                    valueRange = 0f..1f,
                    onValueChange = { viewModel.setDensity(it) },
                    valueDisplay = when {
                        config.density < 0.3f -> "Sparse"
                        config.density < 0.7f -> "Medium"
                        else -> "Busy"
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Chaos/Strangeness Slider
                LabeledSlider(
                    label = "Strangeness",
                    value = config.chaos,
                    valueRange = 0f..1f,
                    onValueChange = { viewModel.setChaos(it) },
                    valueDisplay = when {
                        config.chaos < 0.3f -> "Normal"
                        config.chaos < 0.7f -> "Interesting"
                        else -> "Weird"
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Swing Slider
                LabeledSlider(
                    label = "Swing",
                    value = config.swing,
                    valueRange = 0f..1f,
                    onValueChange = { viewModel.setSwing(it) },
                    valueDisplay = "${(config.swing * 100).toInt()}%"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Advanced toggle button
        OutlinedButton(
            onClick = { showAdvanced = !showAdvanced },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (showAdvanced) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (showAdvanced) "Hide Advanced" else "Show Advanced")
        }

        // Advanced controls
        if (showAdvanced) {
            Spacer(modifier = Modifier.height(16.dp))
            AdvancedGenerativeControls(viewModel, config)
        }
    }
}

/**
 * Advanced generative controls
 */
@Composable
fun AdvancedGenerativeControls(
    viewModel: MainViewModel,
    config: com.dubtechno.generator.data.GenerativeConfig
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Advanced Controls",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pattern lengths
            Text("Pattern Configuration", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bass Pattern", style = MaterialTheme.typography.bodySmall)
                    Text("${config.bassPatternLength} bars", style = MaterialTheme.typography.bodyMedium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Melody Pattern", style = MaterialTheme.typography.bodySmall)
                    Text("${config.melodyPatternLength} bars", style = MaterialTheme.typography.bodyMedium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Chord Progression", style = MaterialTheme.typography.bodySmall)
                    Text("${config.chordProgressionLength} bars", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Euclidean density
            LabeledSlider(
                label = "Euclidean Density",
                value = config.euclideanDensity,
                valueRange = 0f..1f,
                onValueChange = { viewModel.setEuclideanDensity(it) },
                valueDisplay = "${(config.euclideanDensity * 100).toInt()}%"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Note length
            LabeledSlider(
                label = "Note Length",
                value = config.noteLength,
                valueRange = 0f..1f,
                onValueChange = { viewModel.setNoteLength(it) },
                valueDisplay = when {
                    config.noteLength < 0.3f -> "Short"
                    config.noteLength < 0.7f -> "Medium"
                    else -> "Long"
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Velocity variation
            Text(
                "Velocity Variation: ${(config.velocityVariation * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = config.velocityVariation,
                onValueChange = { /* Update in ViewModel */ },
                valueRange = 0f..1f
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Humanize
            LabeledSlider(
                label = "Humanize",
                value = config.humanize,
                valueRange = 0f..1f,
                onValueChange = { viewModel.setHumanize(it) },
                valueDisplay = "${(config.humanize * 100).toInt()}%"
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Polyrhythm settings
            Text("Polyrhythm", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enabled", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = config.polyrhythmEnabled,
                    onCheckedChange = { /* Update in ViewModel */ }
                )
            }

            if (config.polyrhythmEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ratio: ${config.polyrhythmRatio}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Octave settings
            Text("Octave Settings", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bass Octave", style = MaterialTheme.typography.bodySmall)
                    Text("${config.bassOctave}", style = MaterialTheme.typography.bodyLarge)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Melody Octave", style = MaterialTheme.typography.bodySmall)
                    Text("${config.melodyOctave}", style = MaterialTheme.typography.bodyLarge)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Chord Voicing", style = MaterialTheme.typography.bodySmall)
                    Text("${config.chordVoicing} notes", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
