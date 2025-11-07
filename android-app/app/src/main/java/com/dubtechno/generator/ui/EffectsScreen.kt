package com.dubtechno.generator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dubtechno.generator.data.*
import com.dubtechno.generator.ui.components.EffectCard
import com.dubtechno.generator.ui.components.LabeledSlider
import com.dubtechno.generator.viewmodel.MainViewModel

/**
 * Effects screen with all effect processors
 */
@Composable
fun EffectsScreen(viewModel: MainViewModel) {
    val effectSettings by viewModel.effectSettings.collectAsState()
    val synthSettings by viewModel.synthSettings.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Volume
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Master Output",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledSlider(
                        label = "Master Volume",
                        value = synthSettings.masterVolume,
                        valueRange = 0f..1f,
                        onValueChange = { viewModel.setMasterVolume(it) },
                        valueDisplay = "${(synthSettings.masterVolume * 100).toInt()}%"
                    )
                }
            }
        }

        // Tape Echo / Delay
        item {
            EffectCard(
                name = "Tape Echo",
                enabled = effectSettings.delay.enabled,
                onEnabledChange = {
                    viewModel.updateDelay(effectSettings.delay.copy(enabled = it))
                }
            ) {
                val params = effectSettings.delay

                LabeledSlider(
                    label = "Time",
                    value = params.time,
                    valueRange = 0.01f..2f,
                    onValueChange = {
                        viewModel.updateDelay(params.copy(time = it))
                    },
                    valueDisplay = "${(params.time * 1000).toInt()} ms"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Feedback",
                    value = params.feedback,
                    valueRange = 0f..0.9f,
                    onValueChange = {
                        viewModel.updateDelay(params.copy(feedback = it))
                    },
                    valueDisplay = "${(params.feedback * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Wow & Flutter",
                    value = params.wowFlutter,
                    valueRange = 0f..0.5f,
                    onValueChange = {
                        viewModel.updateDelay(params.copy(wowFlutter = it))
                    },
                    valueDisplay = "${(params.wowFlutter * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Filter",
                    value = params.filterCutoff,
                    valueRange = 500f..8000f,
                    onValueChange = {
                        viewModel.updateDelay(params.copy(filterCutoff = it))
                    },
                    valueDisplay = "${params.filterCutoff.toInt()} Hz"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Mix",
                    value = params.mix,
                    valueRange = 0f..1f,
                    onValueChange = {
                        viewModel.updateDelay(params.copy(mix = it))
                    },
                    valueDisplay = "${(params.mix * 100).toInt()}%"
                )
            }
        }

        // Reverb
        item {
            EffectCard(
                name = "Reverb",
                enabled = effectSettings.reverb.enabled,
                onEnabledChange = {
                    viewModel.updateReverb(effectSettings.reverb.copy(enabled = it))
                }
            ) {
                val params = effectSettings.reverb

                LabeledSlider(
                    label = "Size",
                    value = params.size,
                    valueRange = 0f..1f,
                    onValueChange = {
                        viewModel.updateReverb(params.copy(size = it))
                    },
                    valueDisplay = when {
                        params.size < 0.3f -> "Small Room"
                        params.size < 0.6f -> "Medium Hall"
                        else -> "Large Space"
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Decay",
                    value = params.decay,
                    valueRange = 0f..1f,
                    onValueChange = {
                        viewModel.updateReverb(params.copy(decay = it))
                    },
                    valueDisplay = "${(params.decay * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Damping",
                    value = params.damping,
                    valueRange = 0f..1f,
                    onValueChange = {
                        viewModel.updateReverb(params.copy(damping = it))
                    },
                    valueDisplay = "${(params.damping * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Pre-delay",
                    value = params.predelay,
                    valueRange = 0f..0.1f,
                    onValueChange = {
                        viewModel.updateReverb(params.copy(predelay = it))
                    },
                    valueDisplay = "${(params.predelay * 1000).toInt()} ms"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Mix",
                    value = params.mix,
                    valueRange = 0f..1f,
                    onValueChange = {
                        viewModel.updateReverb(params.copy(mix = it))
                    },
                    valueDisplay = "${(params.mix * 100).toInt()}%"
                )
            }
        }

        // Chorus
        item {
            EffectCard(
                name = "Chorus",
                enabled = effectSettings.chorus.enabled,
                onEnabledChange = {
                    viewModel.updateChorus(effectSettings.chorus.copy(enabled = it))
                }
            ) {
                val params = effectSettings.chorus

                LabeledSlider(
                    label = "Rate",
                    value = params.rate,
                    valueRange = 0.1f..5f,
                    onValueChange = {
                        viewModel.updateChorus(params.copy(rate = it))
                    },
                    valueDisplay = "${params.rate} Hz"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Depth",
                    value = params.depth,
                    valueRange = 0f..1f,
                    onValueChange = {
                        viewModel.updateChorus(params.copy(depth = it))
                    },
                    valueDisplay = "${(params.depth * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Mix",
                    value = params.mix,
                    valueRange = 0f..1f,
                    onValueChange = {
                        viewModel.updateChorus(params.copy(mix = it))
                    },
                    valueDisplay = "${(params.mix * 100).toInt()}%"
                )
            }
        }

        // Filter
        item {
            EffectCard(
                name = "Master Filter",
                enabled = effectSettings.filter.enabled,
                onEnabledChange = {
                    viewModel.updateFilter(effectSettings.filter.copy(enabled = it))
                }
            ) {
                val params = effectSettings.filter

                Text("Filter Type", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterType.values().forEach { type ->
                        FilterChip(
                            selected = params.type == type,
                            onClick = {
                                viewModel.updateFilter(params.copy(type = type))
                            },
                            label = { Text(type.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Cutoff",
                    value = params.cutoff,
                    valueRange = 20f..20000f,
                    onValueChange = {
                        viewModel.updateFilter(params.copy(cutoff = it))
                    },
                    valueDisplay = "${params.cutoff.toInt()} Hz"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Resonance",
                    value = params.resonance,
                    valueRange = 0f..1f,
                    onValueChange = {
                        viewModel.updateFilter(params.copy(resonance = it))
                    },
                    valueDisplay = "${(params.resonance * 100).toInt()}%"
                )
            }
        }

        // Compressor
        item {
            EffectCard(
                name = "Compressor",
                enabled = effectSettings.compressor.enabled,
                onEnabledChange = {
                    viewModel.updateCompressor(effectSettings.compressor.copy(enabled = it))
                }
            ) {
                val params = effectSettings.compressor

                LabeledSlider(
                    label = "Threshold",
                    value = params.threshold,
                    valueRange = -40f..0f,
                    onValueChange = {
                        viewModel.updateCompressor(params.copy(threshold = it))
                    },
                    valueDisplay = "${params.threshold.toInt()} dB"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Ratio",
                    value = params.ratio,
                    valueRange = 1f..20f,
                    onValueChange = {
                        viewModel.updateCompressor(params.copy(ratio = it))
                    },
                    valueDisplay = "${params.ratio.toInt()}:1"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Attack: ${(params.attack * 1000).toInt()} ms", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        Text("Release: ${(params.release * 1000).toInt()} ms", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Saturation
        item {
            EffectCard(
                name = "Saturation",
                enabled = effectSettings.saturation.enabled,
                onEnabledChange = {
                    viewModel.updateSaturation(effectSettings.saturation.copy(enabled = it))
                }
            ) {
                val params = effectSettings.saturation

                LabeledSlider(
                    label = "Drive",
                    value = params.drive,
                    valueRange = 0f..1f,
                    onValueChange = {
                        viewModel.updateSaturation(params.copy(drive = it))
                    },
                    valueDisplay = "${(params.drive * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabeledSlider(
                    label = "Mix",
                    value = params.mix,
                    valueRange = 0f..1f,
                    onValueChange = {
                        viewModel.updateSaturation(params.copy(mix = it))
                    },
                    valueDisplay = "${(params.mix * 100).toInt()}%"
                )
            }
        }

        // Bottom padding for scrolling
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
