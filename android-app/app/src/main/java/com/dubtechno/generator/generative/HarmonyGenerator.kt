package com.dubtechno.generator.generative

import com.dubtechno.generator.model.ChordType
import com.dubtechno.generator.model.GenerativeConfig
import com.dubtechno.generator.model.Note
import com.dubtechno.generator.model.Pattern
import com.dubtechno.generator.model.ScaleDegree
import kotlin.random.Random

/**
 * Generates harmonic content (chords, pads) for dub techno
 *
 * Dub techno harmony:
 * - Often static (just root drone)
 * - Very slow chord changes (4-8 bars)
 * - Suspended chords (sus2, sus4) for ambiguity
 * - Minimal voice movement
 */
class HarmonyGenerator(private val config: GenerativeConfig) {

    private val random = config.seed?.let { Random(it) } ?: Random.Default

    /**
     * Generate chord progression
     *
     * Returns list of chord roots (as scale degrees)
     */
    fun generateProgression(
        rootNote: Int,
        scale: List<Int>,
        length: Int = 4
    ): List<List<Int>> {
        val chords = mutableListOf<List<Int>>()

        // Dub techno: mostly static or very simple progressions
        when {
            config.bassMovement < 0.2f -> {
                // Static root chord
                repeat(length) {
                    chords.add(listOf(scale[0]))
                }
            }

            config.bassMovement < 0.5f -> {
                // I - iv progression (very common in dub)
                val root = scale[0]
                val fourth = if (scale.size > 3) scale[3] else root

                for (i in 0 until length) {
                    chords.add(
                        if (i % 2 == 0) listOf(root) else listOf(fourth)
                    )
                }
            }

            config.bassMovement < 0.7f -> {
                // I - v progression
                val root = scale[0]
                val fifth = if (scale.size > 4) scale[4] else root

                for (i in 0 until length) {
                    chords.add(
                        if (i % 2 == 0) listOf(root) else listOf(fifth)
                    )
                }
            }

            else -> {
                // More complex: I - iv - v - I
                val root = scale[0]
                val fourth = if (scale.size > 3) scale[3] else root
                val fifth = if (scale.size > 4) scale[4] else root

                val progression = listOf(root, fourth, fifth, root)
                for (i in 0 until length) {
                    chords.add(listOf(progression[i % progression.size]))
                }
            }
        }

        return chords
    }

    /**
     * Generate pad pattern from chord
     */
    fun generatePad(
        chord: List<Int>,
        rootNote: Int,
        chordType: ChordType = ChordType.MINOR,
        length: Int = 16
    ): Pattern {
        val notes = mutableListOf<Note?>()

        // Generate full chord voicing
        val chordNotes = chordType.getNotes(rootNote + chord[0])

        // Pad: long sustained chord
        // Place chord at start
        notes.add(Note(chordNotes[0], 0.5f, 4.0f)) // Root (long sustain)

        // Fill rest of pattern with nulls (sustain continues)
        repeat(length - 1) {
            notes.add(null)
        }

        return Pattern(notes, length, config.seed)
    }

    /**
     * Generate multi-voice pad (multiple notes sounding together)
     */
    fun generateMultiVoicePad(
        chord: List<Int>,
        rootNote: Int,
        chordType: ChordType = ChordType.SUS2,
        length: Int = 16
    ): List<Pattern> {
        // Generate separate pattern for each voice of the chord
        val chordNotes = chordType.getNotes(rootNote + chord[0])

        return chordNotes.map { note ->
            val notes = mutableListOf<Note?>()

            // Long sustained note
            notes.add(Note(note, 0.4f + (random.nextFloat() * 0.15f), 4.0f))

            // Fill rest
            repeat(length - 1) {
                notes.add(null)
            }

            Pattern(notes, length, config.seed)
        }
    }

    /**
     * Generate arpeggiated chord pattern
     */
    fun generateArpeggio(
        chord: List<Int>,
        rootNote: Int,
        chordType: ChordType = ChordType.MINOR,
        length: Int = 16
    ): Pattern {
        val notes = mutableListOf<Note?>()
        val chordNotes = chordType.getNotes(rootNote + chord[0])

        // Slow arpeggio through chord tones
        for (i in 0 until length) {
            if (i % 4 == 0) { // Quarter notes
                val noteIndex = (i / 4) % chordNotes.size
                val velocity = 0.5f + (random.nextFloat() * 0.1f)
                notes.add(Note(chordNotes[noteIndex], velocity, 1.0f))
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, length, config.seed)
    }

    /**
     * Generate stab pattern (short percussive chords)
     */
    fun generateStab(
        chord: List<Int>,
        rootNote: Int,
        chordType: ChordType = ChordType.MINOR7,
        length: Int = 16
    ): Pattern {
        val notes = mutableListOf<Note?>()
        val chordNotes = chordType.getNotes(rootNote + chord[0])

        for (i in 0 until length) {
            // Place stabs sparsely
            val placeStab = when {
                i % 8 == 0 -> true // Every other beat
                else -> random.nextFloat() < config.density * 0.2f
            }

            if (placeStab) {
                // Use root note of chord for pattern
                // (In actual playback, full chord would sound)
                val velocity = 0.6f + (random.nextFloat() * 0.2f)
                notes.add(Note(chordNotes[0], velocity, 0.125f)) // Short stab
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, length, config.seed)
    }

    /**
     * Generate ambient chord progression (very slow changes)
     */
    fun generateAmbientProgression(
        rootNote: Int,
        scale: List<Int>,
        bars: Int = 8
    ): List<ChordChange> {
        val changes = mutableListOf<ChordChange>()

        // Very slow chord changes
        val root = scale[0]
        val fourth = if (scale.size > 3) scale[3] else root

        // Change every 4 bars
        for (bar in 0 until bars step 4) {
            val chordRoot = if (bar % 8 == 0) root else fourth
            val chordType = if (random.nextFloat() < 0.6f) {
                ChordType.SUS2 // Suspended chords for ambiguity
            } else {
                ChordType.MINOR
            }

            changes.add(
                ChordChange(
                    bar = bar,
                    root = chordRoot,
                    chordType = chordType
                )
            )
        }

        return changes
    }

    /**
     * Select appropriate chord type for dub techno
     */
    fun selectDubChordType(): ChordType {
        val roll = random.nextFloat()
        return when {
            roll < 0.3f -> ChordType.MINOR   // 30% minor
            roll < 0.5f -> ChordType.SUS2    // 20% sus2
            roll < 0.7f -> ChordType.SUS4    // 20% sus4
            roll < 0.85f -> ChordType.MINOR7 // 15% minor7
            roll < 0.95f -> ChordType.POWER  // 10% power chord
            else -> ChordType.ADD9           // 5% add9
        }
    }

    /**
     * Generate drone (sustained root note)
     */
    fun generateDrone(rootNote: Int, scale: List<Int>): Pattern {
        val droneNote = rootNote + scale[0]

        val notes = listOf(
            Note(droneNote, 0.6f, 8.0f) // Very long sustain
        ) + List(15) { null }

        return Pattern(notes, 16, config.seed)
    }

    data class ChordChange(
        val bar: Int,
        val root: Int,
        val chordType: ChordType
    )
}
