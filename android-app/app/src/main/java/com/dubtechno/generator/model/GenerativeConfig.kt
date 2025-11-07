package com.dubtechno.generator.model

/**
 * Configuration for the generative music engine
 * Controls the musical characteristics and evolution behavior
 */
data class GenerativeConfig(
    /** Root MIDI note (60 = middle C, 57 = A, etc.) */
    val rootNote: Int = 60,

    /** Musical scale to use */
    val scale: ScaleType = ScaleType.MINOR,

    /** Tempo in BPM (typical dub techno: 110-130) */
    val tempo: Float = 120f,

    /** Musical density (0.0 = sparse/minimal, 1.0 = busy/maximal) */
    val density: Float = 0.5f,

    /** Chaos/randomness level (0.0 = predictable, 1.0 = chaotic) */
    val chaos: Float = 0.3f,

    /** How fast patterns evolve (0.0 = static, 1.0 = rapidly changing) */
    val evolutionSpeed: Float = 0.5f,

    /** Melody octave range */
    val melodyRange: MelodyRange = MelodyRange.MID,

    /** Rhythm complexity (0.0 = simple, 1.0 = complex polyrhythms) */
    val rhythmComplexity: Float = 0.5f,

    /** Swing amount for drum timing (0.0 = straight, 1.0 = heavy swing) */
    val swing: Float = 0.0f,

    /** Bass movement frequency (0.0 = static drone, 1.0 = active bassline) */
    val bassMovement: Float = 0.3f,

    /** Melody density (0.0 = rare notes, 1.0 = continuous) */
    val melodyDensity: Float = 0.4f,

    /** Use Euclidean rhythms for drums (more even distribution) */
    val useEuclideanDrums: Boolean = true,

    /** Random seed for reproducible generation */
    val seed: Long? = null
) {
    init {
        require(rootNote in 0..127) { "Root note must be valid MIDI note (0-127)" }
        require(tempo in 60f..200f) { "Tempo must be reasonable (60-200 BPM)" }
        require(density in 0f..1f) { "Density must be 0.0-1.0" }
        require(chaos in 0f..1f) { "Chaos must be 0.0-1.0" }
        require(evolutionSpeed in 0f..1f) { "Evolution speed must be 0.0-1.0" }
        require(rhythmComplexity in 0f..1f) { "Rhythm complexity must be 0.0-1.0" }
        require(swing in 0f..1f) { "Swing must be 0.0-1.0" }
        require(bassMovement in 0f..1f) { "Bass movement must be 0.0-1.0" }
        require(melodyDensity in 0f..1f) { "Melody density must be 0.0-1.0" }
    }

    /**
     * Calculate milliseconds per 16th note based on tempo
     */
    fun getMsPerSixteenth(): Long {
        return (60000 / (tempo * 4)).toLong()
    }

    /**
     * Calculate milliseconds per beat
     */
    fun getMsPerBeat(): Long {
        return (60000 / tempo).toLong()
    }

    companion object {
        /** Minimal/ambient preset */
        fun minimal() = GenerativeConfig(
            density = 0.2f,
            chaos = 0.1f,
            evolutionSpeed = 0.2f,
            bassMovement = 0.1f,
            melodyDensity = 0.2f,
            rhythmComplexity = 0.3f
        )

        /** Classic dub techno preset */
        fun classic() = GenerativeConfig(
            density = 0.5f,
            chaos = 0.3f,
            evolutionSpeed = 0.5f,
            bassMovement = 0.3f,
            melodyDensity = 0.4f,
            rhythmComplexity = 0.5f
        )

        /** Experimental/chaotic preset */
        fun experimental() = GenerativeConfig(
            density = 0.7f,
            chaos = 0.7f,
            evolutionSpeed = 0.8f,
            bassMovement = 0.6f,
            melodyDensity = 0.6f,
            rhythmComplexity = 0.8f
        )
    }
}

/**
 * Melody range options
 */
enum class MelodyRange(val octaveOffset: Int) {
    LOW(-1),   // One octave below root
    MID(0),    // Same octave as root
    HIGH(1);   // One octave above root
}
