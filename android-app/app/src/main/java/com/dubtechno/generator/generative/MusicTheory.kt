package com.dubtechno.generator.generative

import com.dubtechno.generator.model.ChordType
import com.dubtechno.generator.model.ScaleType
import kotlin.math.pow

/**
 * Music theory utilities for the generative engine
 */
object MusicTheory {

    /**
     * Get all notes in a scale starting from root
     *
     * @param root Root MIDI note
     * @param scaleType Type of scale
     * @param octaves Number of octaves to generate (default 1)
     * @return List of MIDI notes in the scale
     */
    fun getScaleNotes(root: Int, scaleType: ScaleType, octaves: Int = 1): List<Int> {
        return scaleType.getNotes(root, octaves)
    }

    /**
     * Convert MIDI note number to frequency in Hz
     *
     * Uses standard A440 tuning: A4 (MIDI 69) = 440 Hz
     */
    fun noteToFrequency(midiNote: Int): Float {
        return 440f * 2f.pow((midiNote - 69) / 12f)
    }

    /**
     * Get chord notes from root note, chord type, and scale
     *
     * Ensures all chord notes are in the given scale
     */
    fun getChordNotes(root: Int, chordType: ChordType, scale: List<Int>): List<Int> {
        val chordIntervals = chordType.intervals
        return chordIntervals.map { interval ->
            // Find closest note in scale to the ideal interval
            val idealNote = root + interval
            scale.minByOrNull { Math.abs(it - idealNote) } ?: idealNote
        }
    }

    /**
     * Get interval name from semitones
     */
    fun getIntervalName(semitones: Int): String {
        return when (semitones % 12) {
            0 -> "Unison/Octave"
            1 -> "Minor 2nd"
            2 -> "Major 2nd"
            3 -> "Minor 3rd"
            4 -> "Major 3rd"
            5 -> "Perfect 4th"
            6 -> "Tritone"
            7 -> "Perfect 5th"
            8 -> "Minor 6th"
            9 -> "Major 6th"
            10 -> "Minor 7th"
            11 -> "Major 7th"
            else -> "Unknown"
        }
    }

    /**
     * Quantize a MIDI note to the nearest note in the scale
     */
    fun quantizeToScale(note: Int, scale: List<Int>): Int {
        return scale.minByOrNull { Math.abs(it - note) } ?: note
    }

    /**
     * Get the interval in semitones between two notes
     */
    fun getInterval(note1: Int, note2: Int): Int {
        return Math.abs(note2 - note1)
    }

    /**
     * Check if an interval is consonant (stable/pleasant)
     */
    fun isConsonant(semitones: Int): Boolean {
        val interval = semitones % 12
        return interval in listOf(0, 3, 4, 5, 7, 8, 9, 12)
    }

    /**
     * Get note name from MIDI number
     */
    fun getNoteNamee(midiNote: Int): String {
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val octave = (midiNote / 12) - 1
        val noteName = noteNames[midiNote % 12]
        return "$noteName$octave"
    }

    /**
     * Common dub techno chord progressions
     */
    object DubProgressions {
        /** Static root - most common in dub techno */
        fun staticRoot(root: Int) = listOf(root)

        /** I - iv (root to subdominant) */
        fun rootToSubdominant(scale: List<Int>) = listOf(
            scale[0], // I
            scale[3]  // iv
        )

        /** I - v (root to dominant) */
        fun rootToDominant(scale: List<Int>) = listOf(
            scale[0], // I
            scale[4]  // v
        )

        /** I - bVII (root to subtonic) */
        fun rootToSubtonic(scale: List<Int>) = listOf(
            scale[0], // I
            scale[6]  // bVII
        )

        /** I - iv - v - I (classic progression) */
        fun fourChordLoop(scale: List<Int>) = listOf(
            scale[0], // I
            scale[3], // iv
            scale[4], // v
            scale[0]  // I
        )
    }

    /**
     * Typical dub techno MIDI note ranges
     */
    object Ranges {
        val SUB_BASS = 24..40      // Very low frequencies
        val BASS = 36..55          // Bass range
        val MELODY_LOW = 48..60    // Low melody
        val MELODY_MID = 60..72    // Mid melody (most common)
        val MELODY_HIGH = 72..84   // High melody
        val PAD = 48..72           // Pad/chord range
    }

    /**
     * Calculate stepwise motion probability based on interval
     * Dub techno favors small intervals (seconds, thirds)
     */
    fun getStepwiseProbability(interval: Int): Float {
        return when (Math.abs(interval)) {
            0 -> 0.4f    // Same note (common in dub)
            1, 2 -> 0.8f // Minor/major 2nd (very common)
            3, 4 -> 0.6f // Minor/major 3rd (common)
            5 -> 0.3f    // Perfect 4th (occasional)
            7 -> 0.3f    // Perfect 5th (occasional)
            else -> 0.1f // Larger jumps (rare)
        }
    }
}
