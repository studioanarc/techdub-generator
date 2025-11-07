package com.dubtechno.generator.model

/**
 * Represents a musical pattern - a sequence of notes and rests
 *
 * @param notes List of notes where null = rest/silence
 * @param length Number of steps in the pattern (typically 8, 16, or 32)
 * @param seed Random seed for reproducible pattern generation
 */
data class Pattern(
    val notes: List<Note?>,
    val length: Int,
    val seed: Long? = null
) {
    init {
        require(length > 0) { "Pattern length must be positive, got $length" }
        require(notes.size == length) { "Notes list size (${notes.size}) must match length ($length)" }
    }

    /**
     * Get the note at a specific step (wraps around if index >= length)
     */
    fun getNoteAt(step: Int): Note? {
        return notes[step % length]
    }

    /**
     * Count non-null notes (actual hits)
     */
    fun countHits(): Int {
        return notes.count { it != null }
    }

    /**
     * Get density (ratio of hits to total steps)
     */
    fun getDensity(): Float {
        return countHits().toFloat() / length
    }

    /**
     * Rotate pattern by n steps
     */
    fun rotate(steps: Int): Pattern {
        val rotated = notes.takeLast(steps % length) + notes.dropLast(steps % length)
        return copy(notes = rotated)
    }

    /**
     * Reverse the pattern
     */
    fun reverse(): Pattern {
        return copy(notes = notes.reversed())
    }

    /**
     * Adjust all velocities by a factor
     */
    fun adjustVelocities(factor: Float): Pattern {
        return copy(
            notes = notes.map { note ->
                note?.withVelocity(note.velocity * factor)
            }
        )
    }

    /**
     * Apply probability to all notes
     */
    fun withProbability(probability: Float): Pattern {
        return copy(
            notes = notes.map { note ->
                note?.copy(probability = probability.coerceIn(0f, 1f))
            }
        )
    }

    companion object {
        /**
         * Create an empty pattern (all rests)
         */
        fun empty(length: Int): Pattern {
            return Pattern(
                notes = List(length) { null },
                length = length
            )
        }

        /**
         * Create a simple pattern from a rhythm boolean array
         */
        fun fromRhythm(rhythm: BooleanArray, note: Int, velocity: Float = 0.7f): Pattern {
            return Pattern(
                notes = rhythm.map { hit ->
                    if (hit) Note(note, velocity) else null
                },
                length = rhythm.size
            )
        }
    }
}
