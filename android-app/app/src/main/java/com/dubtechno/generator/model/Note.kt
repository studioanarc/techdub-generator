package com.dubtechno.generator.model

/**
 * Represents a musical note with MIDI properties
 *
 * @param midiNote MIDI note number (0-127), where 60 = middle C
 * @param velocity Note intensity (0.0-1.0), affects volume and timbre
 * @param duration Note length in beats (0.25 = 16th note, 0.5 = 8th note, etc.)
 * @param probability Chance this note will trigger (0.0-1.0), enables probabilistic rhythms
 */
data class Note(
    val midiNote: Int,
    val velocity: Float = 0.7f,
    val duration: Float = 0.25f,
    val probability: Float = 1.0f
) {
    init {
        require(midiNote in 0..127) { "MIDI note must be 0-127, got $midiNote" }
        require(velocity in 0f..1f) { "Velocity must be 0.0-1.0, got $velocity" }
        require(duration > 0f) { "Duration must be positive, got $duration" }
        require(probability in 0f..1f) { "Probability must be 0.0-1.0, got $probability" }
    }

    /**
     * Convert MIDI note to frequency in Hz
     */
    fun toFrequency(): Float {
        return 440f * Math.pow(2.0, (midiNote - 69) / 12.0).toFloat()
    }

    /**
     * Create a copy with adjusted velocity
     */
    fun withVelocity(newVelocity: Float): Note {
        return copy(velocity = newVelocity.coerceIn(0f, 1f))
    }

    /**
     * Create a copy transposed by semitones
     */
    fun transpose(semitones: Int): Note {
        return copy(midiNote = (midiNote + semitones).coerceIn(0, 127))
    }
}
