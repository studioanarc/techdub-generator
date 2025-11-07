package com.dubtechno.generator.audio

import com.dubtechno.generator.data.*

/**
 * AudioEngine - JNI wrapper for native C++ audio engine
 * This class interfaces with the native audio processing code
 */
class AudioEngine private constructor() {

    // Native method declarations
    private external fun nativeInitialize(sampleRate: Int, bufferSize: Int): Boolean
    private external fun nativeStart()
    private external fun nativeStop()
    private external fun nativeShutdown()
    private external fun nativeSetMasterVolume(volume: Float)
    private external fun nativeGetWaveformData(): FloatArray

    // Synth parameter updates
    private external fun nativeUpdateBassSynth(
        waveform: Int,
        filterCutoff: Float,
        filterResonance: Float,
        filterEnvAmount: Float,
        attack: Float,
        decay: Float,
        sustain: Float,
        release: Float,
        distortion: Float,
        subOscLevel: Float,
        glide: Float,
        volume: Float
    )

    private external fun nativeUpdateMelodySynth(
        waveform: Int,
        filterCutoff: Float,
        filterResonance: Float,
        filterEnvAmount: Float,
        attack: Float,
        decay: Float,
        sustain: Float,
        release: Float,
        detuneAmount: Float,
        vibratoRate: Float,
        vibratoDepth: Float,
        volume: Float
    )

    private external fun nativeUpdateChordSynth(
        waveform: Int,
        filterCutoff: Float,
        filterResonance: Float,
        attack: Float,
        decay: Float,
        sustain: Float,
        release: Float,
        chorusDepth: Float,
        volume: Float
    )

    // Effect parameter updates
    private external fun nativeUpdateDelay(
        enabled: Boolean,
        time: Float,
        feedback: Float,
        mix: Float,
        wowFlutter: Float,
        filterCutoff: Float
    )

    private external fun nativeUpdateReverb(
        enabled: Boolean,
        size: Float,
        decay: Float,
        damping: Float,
        mix: Float,
        predelay: Float
    )

    private external fun nativeUpdateChorus(
        enabled: Boolean,
        rate: Float,
        depth: Float,
        mix: Float
    )

    private external fun nativeUpdateFilter(
        enabled: Boolean,
        type: Int,
        cutoff: Float,
        resonance: Float
    )

    private external fun nativeUpdateCompressor(
        enabled: Boolean,
        threshold: Float,
        ratio: Float,
        attack: Float,
        release: Float
    )

    private external fun nativeUpdateSaturation(
        enabled: Boolean,
        drive: Float,
        mix: Float
    )

    // Note triggering (called by GenerativeEngine)
    external fun triggerNote(
        synthId: Int, // 0=bass, 1=melody, 2=chords
        midiNote: Int,
        velocity: Float,
        duration: Float
    )

    external fun stopNote(synthId: Int, midiNote: Int)

    // Public API
    fun initialize(sampleRate: Int, bufferSize: Int): Boolean {
        return try {
            nativeInitialize(sampleRate, bufferSize)
        } catch (e: UnsatisfiedLinkError) {
            // Native library not loaded, return false
            false
        }
    }

    fun start() {
        try {
            nativeStart()
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun stop() {
        try {
            nativeStop()
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun shutdown() {
        try {
            nativeShutdown()
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun setMasterVolume(volume: Float) {
        try {
            nativeSetMasterVolume(volume)
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun getWaveformData(): FloatArray {
        return try {
            nativeGetWaveformData()
        } catch (e: UnsatisfiedLinkError) {
            // Return empty waveform
            FloatArray(256) { 0f }
        }
    }

    fun updateBassSynth(params: BassSynthParams) {
        try {
            nativeUpdateBassSynth(
                params.waveform.ordinal,
                params.filterCutoff,
                params.filterResonance,
                params.filterEnvAmount,
                params.attack,
                params.decay,
                params.sustain,
                params.release,
                params.distortion,
                params.subOscLevel,
                params.glide,
                params.volume
            )
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun updateMelodySynth(params: MelodySynthParams) {
        try {
            nativeUpdateMelodySynth(
                params.waveform.ordinal,
                params.filterCutoff,
                params.filterResonance,
                params.filterEnvAmount,
                params.attack,
                params.decay,
                params.sustain,
                params.release,
                params.detuneAmount,
                params.vibratoRate,
                params.vibratoDepth,
                params.volume
            )
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun updateChordSynth(params: ChordSynthParams) {
        try {
            nativeUpdateChordSynth(
                params.waveform.ordinal,
                params.filterCutoff,
                params.filterResonance,
                params.attack,
                params.decay,
                params.sustain,
                params.release,
                params.chorusDepth,
                params.volume
            )
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun updateDelay(params: DelayParams) {
        try {
            nativeUpdateDelay(
                params.enabled,
                params.time,
                params.feedback,
                params.mix,
                params.wowFlutter,
                params.filterCutoff
            )
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun updateReverb(params: ReverbParams) {
        try {
            nativeUpdateReverb(
                params.enabled,
                params.size,
                params.decay,
                params.damping,
                params.mix,
                params.predelay
            )
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun updateChorus(params: ChorusParams) {
        try {
            nativeUpdateChorus(
                params.enabled,
                params.rate,
                params.depth,
                params.mix
            )
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun updateFilter(params: FilterParams) {
        try {
            nativeUpdateFilter(
                params.enabled,
                params.type.ordinal,
                params.cutoff,
                params.resonance
            )
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun updateCompressor(params: CompressorParams) {
        try {
            nativeUpdateCompressor(
                params.enabled,
                params.threshold,
                params.ratio,
                params.attack,
                params.release
            )
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    fun updateSaturation(params: SaturationParams) {
        try {
            nativeUpdateSaturation(
                params.enabled,
                params.drive,
                params.mix
            )
        } catch (e: UnsatisfiedLinkError) {
            // Fallback: do nothing
        }
    }

    companion object {
        private var instance: AudioEngine? = null

        init {
            try {
                System.loadLibrary("dubtechno")
            } catch (e: UnsatisfiedLinkError) {
                // Native library not available
                e.printStackTrace()
            }
        }

        fun getInstance(): AudioEngine {
            if (instance == null) {
                instance = AudioEngine()
            }
            return instance!!
        }
    }
}
