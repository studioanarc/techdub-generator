package com.dubtechno.generator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dubtechno.generator.data.BassSynthParams
import com.dubtechno.generator.data.ChordSynthParams
import com.dubtechno.generator.data.MelodySynthParams
import com.dubtechno.generator.data.Waveform
import com.dubtechno.generator.ui.components.LabeledSlider
import com.dubtechno.generator.ui.components.WaveformSelector
import com.dubtechno.generator.viewmodel.MainViewModel

/**
 * Synth controls screen with tabs for Bass, Melody, and Chords
 */
@Composable
fun SynthControlsScreen(viewModel: MainViewModel) {
    var selectedSynth by remember { mutableStateOf(0) }
    val synthSettings by viewModel.synthSettings.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Synth selector tabs
        TabRow(selectedTabIndex = selectedSynth) {
            Tab(
                selected = selectedSynth == 0,
                onClick = { selectedSynth = 0 },
                text = { Text("Bass") }
            )
            Tab(
                selected = selectedSynth == 1,
                onClick = { selectedSynth = 1 },
                text = { Text("Melody") }
            )
            Tab(
                selected = selectedSynth == 2,
                onClick = { selectedSynth = 2 },
                text = { Text("Chords") }
            )
        }

        // Synth controls
        when (selectedSynth) {
            0 -> BassSynthControls(
                params = synthSettings.bass,
                onUpdate = { viewModel.updateBassSynth(it) }
            )
            1 -> MelodySynthControls(
                params = synthSettings.melody,
                onUpdate = { viewModel.updateMelodySynth(it) }
            )
            2 -> ChordSynthControls(
                params = synthSettings.chords,
                onUpdate = { viewModel.updateChordSynth(it) }
            )
        }
    }
}

/**
 * Bass synth controls
 */
@Composable
fun BassSynthControls(
    params: BassSynthParams,
    onUpdate: (BassSynthParams) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Oscillator Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Oscillator",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                WaveformSelector(
                    selectedWaveform = params.waveform,
                    onWaveformSelected = { onUpdate(params.copy(waveform = it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Sub Oscillator",
                    value = params.subOscLevel,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(subOscLevel = it)) },
                    valueDisplay = "${(params.subOscLevel * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Glide/Portamento",
                    value = params.glide,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(glide = it)) },
                    valueDisplay = "${(params.glide * 1000).toInt()} ms"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Filter",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Cutoff",
                    value = params.filterCutoff,
                    valueRange = 20f..10000f,
                    onValueChange = { onUpdate(params.copy(filterCutoff = it)) },
                    valueDisplay = "${params.filterCutoff.toInt()} Hz"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Resonance",
                    value = params.filterResonance,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(filterResonance = it)) },
                    valueDisplay = "${(params.filterResonance * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Envelope Amount",
                    value = params.filterEnvAmount,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(filterEnvAmount = it)) },
                    valueDisplay = "${(params.filterEnvAmount * 100).toInt()}%"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Envelope Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Amplitude Envelope",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        LabeledSlider(
                            label = "Attack",
                            value = params.attack,
                            valueRange = 0.001f..2f,
                            onValueChange = { onUpdate(params.copy(attack = it)) },
                            valueDisplay = "${(params.attack * 1000).toInt()} ms"
                        )
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        LabeledSlider(
                            label = "Decay",
                            value = params.decay,
                            valueRange = 0.001f..2f,
                            onValueChange = { onUpdate(params.copy(decay = it)) },
                            valueDisplay = "${(params.decay * 1000).toInt()} ms"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        LabeledSlider(
                            label = "Sustain",
                            value = params.sustain,
                            valueRange = 0f..1f,
                            onValueChange = { onUpdate(params.copy(sustain = it)) },
                            valueDisplay = "${(params.sustain * 100).toInt()}%"
                        )
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        LabeledSlider(
                            label = "Release",
                            value = params.release,
                            valueRange = 0.001f..3f,
                            onValueChange = { onUpdate(params.copy(release = it)) },
                            valueDisplay = "${(params.release * 1000).toInt()} ms"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Effects Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Effects",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Distortion",
                    value = params.distortion,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(distortion = it)) },
                    valueDisplay = "${(params.distortion * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Volume",
                    value = params.volume,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(volume = it)) },
                    valueDisplay = "${(params.volume * 100).toInt()}%"
                )
            }
        }
    }
}

/**
 * Melody synth controls
 */
@Composable
fun MelodySynthControls(
    params: MelodySynthParams,
    onUpdate: (MelodySynthParams) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Oscillator Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Oscillator",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                WaveformSelector(
                    selectedWaveform = params.waveform,
                    onWaveformSelected = { onUpdate(params.copy(waveform = it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Detune",
                    value = params.detuneAmount,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(detuneAmount = it)) },
                    valueDisplay = "${(params.detuneAmount * 100).toInt()}%"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vibrato Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Vibrato",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Rate",
                    value = params.vibratoRate,
                    valueRange = 0.1f..10f,
                    onValueChange = { onUpdate(params.copy(vibratoRate = it)) },
                    valueDisplay = "${params.vibratoRate} Hz"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Depth",
                    value = params.vibratoDepth,
                    valueRange = 0f..0.2f,
                    onValueChange = { onUpdate(params.copy(vibratoDepth = it)) },
                    valueDisplay = "${(params.vibratoDepth * 100).toInt()}%"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Filter & Envelope",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Cutoff",
                    value = params.filterCutoff,
                    valueRange = 20f..10000f,
                    onValueChange = { onUpdate(params.copy(filterCutoff = it)) },
                    valueDisplay = "${params.filterCutoff.toInt()} Hz"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Resonance",
                    value = params.filterResonance,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(filterResonance = it)) },
                    valueDisplay = "${(params.filterResonance * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ADSR in compact grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                        Text("A: ${(params.attack * 1000).toInt()}ms", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                        Text("D: ${(params.decay * 1000).toInt()}ms", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                        Text("S: ${(params.sustain * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                        Text("R: ${(params.release * 1000).toInt()}ms", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Volume",
                    value = params.volume,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(volume = it)) },
                    valueDisplay = "${(params.volume * 100).toInt()}%"
                )
            }
        }
    }
}

/**
 * Chord synth controls
 */
@Composable
fun ChordSynthControls(
    params: ChordSynthParams,
    onUpdate: (ChordSynthParams) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Pad Synth",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                WaveformSelector(
                    selectedWaveform = params.waveform,
                    onWaveformSelected = { onUpdate(params.copy(waveform = it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Filter Cutoff",
                    value = params.filterCutoff,
                    valueRange = 20f..10000f,
                    onValueChange = { onUpdate(params.copy(filterCutoff = it)) },
                    valueDisplay = "${params.filterCutoff.toInt()} Hz"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Resonance",
                    value = params.filterResonance,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(filterResonance = it)) },
                    valueDisplay = "${(params.filterResonance * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Envelope (A/D/S/R)", style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${(params.attack * 1000).toInt()}ms", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${(params.decay * 1000).toInt()}ms", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${(params.sustain * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${(params.release * 1000).toInt()}ms", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Chorus Depth",
                    value = params.chorusDepth,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(chorusDepth = it)) },
                    valueDisplay = "${(params.chorusDepth * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabeledSlider(
                    label = "Volume",
                    value = params.volume,
                    valueRange = 0f..1f,
                    onValueChange = { onUpdate(params.copy(volume = it)) },
                    valueDisplay = "${(params.volume * 100).toInt()}%"
                )
            }
        }
    }
}
