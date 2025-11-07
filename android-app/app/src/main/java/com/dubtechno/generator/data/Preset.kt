package com.dubtechno.generator.data

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a complete preset including generative, synth, and effect settings
 */
@Serializable
data class Preset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val config: GenerativeConfig,
    val synthSettings: SynthSettings,
    val effectSettings: EffectSettings
)

/**
 * Generative algorithm configuration
 */
@Serializable
data class GenerativeConfig(
    val tempo: Float = 120f,
    val rootNote: Int = 60, // MIDI note number (C4)
    val scale: ScaleType = ScaleType.MINOR,
    val density: Float = 0.5f, // 0-1: note density
    val chaos: Float = 0.3f, // 0-1: randomness/strangeness
    val swing: Float = 0.5f, // 0-1: timing swing
    val bassPatternLength: Int = 4, // bars
    val melodyPatternLength: Int = 8, // bars
    val chordProgressionLength: Int = 8, // bars
    val bassOctave: Int = 2,
    val melodyOctave: Int = 4,
    val chordVoicing: Int = 3, // number of notes in chords

    // Advanced parameters
    val euclideanDensity: Float = 0.7f,
    val noteLength: Float = 0.5f, // 0-1: short to long
    val velocityVariation: Float = 0.3f,
    val humanize: Float = 0.2f, // timing humanization
    val polyrhythmEnabled: Boolean = false,
    val polyrhythmRatio: String = "3:4"
)

/**
 * Synth parameters for all synth engines
 */
@Serializable
data class SynthSettings(
    val bass: BassSynthParams = BassSynthParams(),
    val melody: MelodySynthParams = MelodySynthParams(),
    val chords: ChordSynthParams = ChordSynthParams(),
    val masterVolume: Float = 0.7f
)

@Serializable
data class BassSynthParams(
    val waveform: Waveform = Waveform.SAWTOOTH,
    val filterCutoff: Float = 800f, // Hz
    val filterResonance: Float = 0.5f,
    val filterEnvAmount: Float = 0.6f,
    val attack: Float = 0.01f, // seconds
    val decay: Float = 0.3f,
    val sustain: Float = 0.7f,
    val release: Float = 0.5f,
    val distortion: Float = 0.3f,
    val subOscLevel: Float = 0.4f,
    val glide: Float = 0.1f, // portamento time
    val volume: Float = 0.8f
)

@Serializable
data class MelodySynthParams(
    val waveform: Waveform = Waveform.SQUARE,
    val filterCutoff: Float = 2000f,
    val filterResonance: Float = 0.3f,
    val filterEnvAmount: Float = 0.5f,
    val attack: Float = 0.05f,
    val decay: Float = 0.2f,
    val sustain: Float = 0.6f,
    val release: Float = 0.8f,
    val detuneAmount: Float = 0.1f,
    val vibratoRate: Float = 5f, // Hz
    val vibratoDepth: Float = 0.05f,
    val volume: Float = 0.6f
)

@Serializable
data class ChordSynthParams(
    val waveform: Waveform = Waveform.TRIANGLE,
    val filterCutoff: Float = 1500f,
    val filterResonance: Float = 0.2f,
    val attack: Float = 0.5f,
    val decay: Float = 0.3f,
    val sustain: Float = 0.8f,
    val release: Float = 1.5f,
    val chorusDepth: Float = 0.3f,
    val volume: Float = 0.5f
)

/**
 * Effect chain settings
 */
@Serializable
data class EffectSettings(
    val delay: DelayParams = DelayParams(),
    val reverb: ReverbParams = ReverbParams(),
    val chorus: ChorusParams = ChorusParams(),
    val filter: FilterParams = FilterParams(),
    val compressor: CompressorParams = CompressorParams(),
    val saturation: SaturationParams = SaturationParams()
)

@Serializable
data class DelayParams(
    val enabled: Boolean = true,
    val time: Float = 0.375f, // seconds (dotted eighth at 120 BPM)
    val feedback: Float = 0.4f,
    val mix: Float = 0.3f,
    val wowFlutter: Float = 0.15f, // tape simulation
    val filterCutoff: Float = 3000f
)

@Serializable
data class ReverbParams(
    val enabled: Boolean = true,
    val size: Float = 0.7f,
    val decay: Float = 0.6f,
    val damping: Float = 0.5f,
    val mix: Float = 0.25f,
    val predelay: Float = 0.02f
)

@Serializable
data class ChorusParams(
    val enabled: Boolean = true,
    val rate: Float = 0.5f, // Hz
    val depth: Float = 0.3f,
    val mix: Float = 0.2f
)

@Serializable
data class FilterParams(
    val enabled: Boolean = true,
    val type: FilterType = FilterType.LOWPASS,
    val cutoff: Float = 8000f,
    val resonance: Float = 0.3f
)

@Serializable
data class CompressorParams(
    val enabled: Boolean = true,
    val threshold: Float = -12f, // dB
    val ratio: Float = 4f,
    val attack: Float = 0.01f,
    val release: Float = 0.1f
)

@Serializable
data class SaturationParams(
    val enabled: Boolean = true,
    val drive: Float = 0.3f,
    val mix: Float = 0.5f
)

/**
 * Enums for various parameters
 */
@Serializable
enum class ScaleType {
    MINOR,
    DORIAN,
    PHRYGIAN,
    HARMONIC_MINOR,
    MELODIC_MINOR,
    MAJOR,
    MIXOLYDIAN,
    PENTATONIC_MINOR,
    BLUES
}

@Serializable
enum class Waveform {
    SINE,
    TRIANGLE,
    SAWTOOTH,
    SQUARE,
    PULSE
}

@Serializable
enum class FilterType {
    LOWPASS,
    HIGHPASS,
    BANDPASS,
    NOTCH
}

/**
 * Factory presets
 */
object PresetFactory {
    fun createDeepDubPreset() = Preset(
        name = "Deep Dub",
        config = GenerativeConfig(
            tempo = 120f,
            rootNote = 60, // C
            scale = ScaleType.MINOR,
            density = 0.4f,
            chaos = 0.2f,
            swing = 0.6f
        ),
        synthSettings = SynthSettings(
            bass = BassSynthParams(
                filterCutoff = 600f,
                distortion = 0.4f,
                subOscLevel = 0.6f
            ),
            melody = MelodySynthParams(
                filterCutoff = 2500f,
                vibratoDepth = 0.03f
            )
        ),
        effectSettings = EffectSettings(
            delay = DelayParams(
                time = 0.375f,
                feedback = 0.5f,
                wowFlutter = 0.2f
            ),
            reverb = ReverbParams(
                size = 0.8f,
                decay = 0.7f
            )
        )
    )

    fun createMinimalTechnoPreset() = Preset(
        name = "Minimal Techno",
        config = GenerativeConfig(
            tempo = 128f,
            rootNote = 69, // A
            scale = ScaleType.DORIAN,
            density = 0.3f,
            chaos = 0.4f,
            swing = 0.5f
        ),
        synthSettings = SynthSettings(
            bass = BassSynthParams(
                filterCutoff = 800f,
                filterResonance = 0.7f,
                distortion = 0.2f
            )
        ),
        effectSettings = EffectSettings(
            delay = DelayParams(
                time = 0.25f,
                feedback = 0.3f
            ),
            reverb = ReverbParams(
                size = 0.5f,
                mix = 0.15f
            )
        )
    )

    fun createAmbientDubPreset() = Preset(
        name = "Ambient Dub",
        config = GenerativeConfig(
            tempo = 110f,
            rootNote = 62, // D
            scale = ScaleType.PHRYGIAN,
            density = 0.6f,
            chaos = 0.5f,
            swing = 0.55f
        ),
        synthSettings = SynthSettings(
            bass = BassSynthParams(
                attack = 0.1f,
                filterCutoff = 500f
            ),
            melody = MelodySynthParams(
                attack = 0.2f,
                release = 1.5f,
                vibratoDepth = 0.08f
            ),
            chords = ChordSynthParams(
                attack = 0.8f,
                release = 2.0f,
                chorusDepth = 0.4f
            )
        ),
        effectSettings = EffectSettings(
            delay = DelayParams(
                time = 0.5f,
                feedback = 0.6f,
                wowFlutter = 0.25f
            ),
            reverb = ReverbParams(
                size = 0.9f,
                decay = 0.8f,
                mix = 0.4f
            )
        )
    )

    fun getAllFactoryPresets() = listOf(
        createDeepDubPreset(),
        createMinimalTechnoPreset(),
        createAmbientDubPreset()
    )
}
