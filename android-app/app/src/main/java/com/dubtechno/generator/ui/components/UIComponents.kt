package com.dubtechno.generator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dubtechno.generator.data.ScaleType
import com.dubtechno.generator.data.Waveform
import com.dubtechno.generator.ui.theme.GlassBorder
import com.dubtechno.generator.ui.theme.GlassOverlay

/**
 * Labeled slider component with value display
 */
@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueDisplay: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    valueDisplay,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

/**
 * Note selector (C, C#, D, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSelector(
    selectedNote: Int,
    onNoteSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val notes = listOf(
        60 to "C",
        61 to "C#",
        62 to "D",
        63 to "D#",
        64 to "E",
        65 to "F",
        66 to "F#",
        67 to "G",
        68 to "G#",
        69 to "A",
        70 to "A#",
        71 to "B"
    )

    val selectedNoteName = notes.find { it.first == selectedNote }?.second ?: "C"
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedNoteName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            notes.forEach { (midiNote, noteName) ->
                DropdownMenuItem(
                    text = { Text(noteName) },
                    onClick = {
                        onNoteSelected(midiNote)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Scale selector dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaleSelector(
    selectedScale: ScaleType,
    onScaleSelected: (ScaleType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleDisplayNames = mapOf(
        ScaleType.MINOR to "Natural Minor",
        ScaleType.DORIAN to "Dorian",
        ScaleType.PHRYGIAN to "Phrygian",
        ScaleType.HARMONIC_MINOR to "Harmonic Minor",
        ScaleType.MELODIC_MINOR to "Melodic Minor",
        ScaleType.MAJOR to "Major",
        ScaleType.MIXOLYDIAN to "Mixolydian",
        ScaleType.PENTATONIC_MINOR to "Pentatonic Minor",
        ScaleType.BLUES to "Blues"
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = scaleDisplayNames[selectedScale] ?: "Minor",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ScaleType.values().forEach { scale ->
                DropdownMenuItem(
                    text = { Text(scaleDisplayNames[scale] ?: scale.name) },
                    onClick = {
                        onScaleSelected(scale)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Waveform selector
 */
@Composable
fun WaveformSelector(
    selectedWaveform: Waveform,
    onWaveformSelected: (Waveform) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Waveform", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Waveform.values().forEach { waveform ->
                FilterChip(
                    selected = selectedWaveform == waveform,
                    onClick = { onWaveformSelected(waveform) },
                    label = {
                        Text(
                            waveform.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Effect card with enable/disable switch
 */
@Composable
fun EffectCard(
    name: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with name and switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (enabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            // Show controls only when enabled
            if (enabled) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

/**
 * Glass morphism container
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassOverlay,
                        GlassOverlay.copy(alpha = 0.2f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

/**
 * Pulsing indicator for active state
 */
@Composable
fun PulsingIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    if (isActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Box(
            modifier = modifier
                .size(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = alpha))
        )
    }
}

/**
 * Loading indicator
 */
@Composable
fun LoadingIndicator(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
