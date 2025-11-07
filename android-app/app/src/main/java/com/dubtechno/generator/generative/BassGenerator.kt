package com.dubtechno.generator.generative

import com.dubtechno.generator.model.GenerativeConfig
import com.dubtechno.generator.model.Note
import com.dubtechno.generator.model.Pattern
import kotlin.random.Random

/**
 * Generates bass patterns for dub techno
 *
 * Dub techno bass characteristics:
 * - Deep, sub-frequency focused
 * - Minimal movement (mostly root note)
 * - Long sustained notes
 * - Occasional movement to 4th or 5th
 * - Sometimes syncopated or off-beat
 */
class BassGenerator(private val config: GenerativeConfig) {

    private val random = config.seed?.let { Random(it) } ?: Random.Default

    /**
     * Generate main bass pattern
     */
    fun generatePattern(
        rootNote: Int,
        scale: List<Int>,
        length: Int = 16
    ): Pattern {
        val notes = mutableListOf<Note?>()

        // Dub techno bass: mostly on root with occasional movement
        val bassMovementFrequency = config.bassMovement
        val root = scale[0]
        val fifth = if (scale.size > 4) scale[4] else root
        val fourth = if (scale.size > 3) scale[3] else root

        for (i in 0 until length) {
            // Determine if we should place a note at this step
            val shouldPlaceNote = when (i % 4) {
                0 -> true // On the beat
                2 -> config.density > 0.5f // Off-beat if higher density
                else -> random.nextFloat() < config.density * 0.3f // Occasional other placements
            }

            if (shouldPlaceNote) {
                // Determine which note to use
                val note = when {
                    // Mostly root note
                    random.nextFloat() > bassMovementFrequency -> rootNote + root

                    // Occasional movement
                    else -> {
                        when (random.nextInt(3)) {
                            0 -> rootNote + fifth  // Move to fifth
                            1 -> rootNote + fourth // Move to fourth
                            else -> rootNote + root
                        }
                    }
                }

                val velocity = 0.7f + (random.nextFloat() - 0.5f) * 0.1f
                val duration = if (config.density < 0.5f) 1.0f else 0.5f // Longer notes when sparse

                notes.add(Note(note, velocity.coerceIn(0.5f, 0.9f), duration))
            } else {
                notes.add(null) // Rest
            }
        }

        // Ensure we have at least one note on the first beat
        if (notes[0] == null) {
            notes[0] = Note(rootNote + root, 0.8f, 1.0f)
        }

        return Pattern(notes, length, config.seed)
    }

    /**
     * Generate sub-bass (ultra-low drone)
     */
    fun generateSubBass(rootNote: Int, scale: List<Int>): Pattern {
        // Sub-bass: just sustain the root very low
        val subBassNote = rootNote + scale[0] - 24 // Two octaves down

        val notes = listOf(
            Note(subBassNote, 0.6f, 4.0f), // Long sustained note
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null
        )

        return Pattern(notes, 16, config.seed)
    }

    /**
     * Generate walking bass pattern (more movement)
     */
    fun generateWalkingBass(
        rootNote: Int,
        scale: List<Int>,
        length: Int = 16
    ): Pattern {
        val notes = mutableListOf<Note?>()

        // Walking bass moves through scale degrees
        val scaleIndices = listOf(0, 2, 4, 3, 0, 4, 2, 1) // I-III-V-IV-I-V-III-II pattern

        for (i in 0 until length) {
            if (i % 2 == 0) { // Place notes on 8th notes
                val scaleIndex = scaleIndices[(i / 2) % scaleIndices.size]
                val note = if (scaleIndex < scale.size) {
                    rootNote + scale[scaleIndex]
                } else {
                    rootNote + scale[0]
                }

                val velocity = 0.7f + (random.nextFloat() - 0.5f) * 0.1f
                notes.add(Note(note, velocity.coerceIn(0.6f, 0.8f), 0.5f))
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, length, config.seed)
    }

    /**
     * Generate syncopated bass pattern
     */
    fun generateSyncopated(
        rootNote: Int,
        scale: List<Int>,
        length: Int = 16
    ): Pattern {
        val notes = mutableListOf<Note?>()
        val root = scale[0]
        val fifth = if (scale.size > 4) scale[4] else root

        // Syncopated pattern with off-beat emphasis
        val syncopatedPattern = listOf(
            true, false, false, true,  // 1 . . 4
            false, false, true, false, // . . 7 .
            true, false, false, false, // 9 . . .
            false, true, false, false  // . 14 . .
        )

        for (i in 0 until length) {
            if (syncopatedPattern[i % syncopatedPattern.size]) {
                val note = if (random.nextFloat() < 0.7f) {
                    rootNote + root
                } else {
                    rootNote + fifth
                }

                val velocity = if (i % 4 == 0) 0.8f else 0.6f // Accent on-beat notes
                notes.add(Note(note, velocity, 0.25f))
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, length, config.seed)
    }

    /**
     * Generate minimal drone bass (most static)
     */
    fun generateDrone(rootNote: Int, scale: List<Int>): Pattern {
        // Just one long held root note
        val droneNote = rootNote + scale[0]

        val notes = mutableListOf<Note?>()
        notes.add(Note(droneNote, 0.7f, 4.0f)) // Very long note

        // Fill rest with nulls
        repeat(15) { notes.add(null) }

        return Pattern(notes, 16, config.seed)
    }

    /**
     * Choose appropriate bass style based on config
     */
    fun generateAdaptive(
        rootNote: Int,
        scale: List<Int>,
        length: Int = 16
    ): Pattern {
        return when {
            config.bassMovement < 0.2f -> generateDrone(rootNote, scale)
            config.bassMovement < 0.5f -> generatePattern(rootNote, scale, length)
            config.bassMovement < 0.8f -> generateSyncopated(rootNote, scale, length)
            else -> generateWalkingBass(rootNote, scale, length)
        }
    }
}
