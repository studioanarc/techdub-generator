package com.dubtechno.generator.model

/**
 * Musical scale types commonly used in dub techno
 */
enum class ScaleType(val intervals: IntArray) {
    /** Natural minor scale - dark, melancholic (W-H-W-W-H-W-W) */
    MINOR(intArrayOf(0, 2, 3, 5, 7, 8, 10)),

    /** Dorian mode - jazzy, sophisticated (W-H-W-W-W-H-W) */
    DORIAN(intArrayOf(0, 2, 3, 5, 7, 9, 10)),

    /** Phrygian mode - dark, exotic (H-W-W-W-H-W-W) */
    PHRYGIAN(intArrayOf(0, 1, 3, 5, 7, 8, 10)),

    /** Minor pentatonic - safe, bluesy (3H-W-W-3H-W) */
    MINOR_PENTATONIC(intArrayOf(0, 3, 5, 7, 10)),

    /** Whole tone scale - dreamy, ambiguous (W-W-W-W-W-W) */
    WHOLE_TONE(intArrayOf(0, 2, 4, 6, 8, 10));

    /**
     * Get all notes in this scale starting from root
     */
    fun getNotes(root: Int, octaves: Int = 1): List<Int> {
        val notes = mutableListOf<Int>()
        for (octave in 0 until octaves) {
            for (interval in intervals) {
                notes.add(root + interval + (octave * 12))
            }
        }
        return notes
    }
}

/**
 * Chord types for harmony generation
 */
enum class ChordType(val intervals: IntArray) {
    /** Minor triad - sad, dark (root, minor 3rd, perfect 5th) */
    MINOR(intArrayOf(0, 3, 7)),

    /** Minor 7th - jazzy, complex (root, m3, P5, m7) */
    MINOR7(intArrayOf(0, 3, 7, 10)),

    /** Sus2 - open, airy (root, major 2nd, perfect 5th) */
    SUS2(intArrayOf(0, 2, 7)),

    /** Sus4 - suspended, unresolved (root, perfect 4th, perfect 5th) */
    SUS4(intArrayOf(0, 5, 7)),

    /** Add9 - lush, colorful (root, M3, P5, M9) */
    ADD9(intArrayOf(0, 4, 7, 14)),

    /** Power chord - heavy, neutral (root, perfect 5th) */
    POWER(intArrayOf(0, 7));

    /**
     * Get chord notes from root
     */
    fun getNotes(root: Int): List<Int> {
        return intervals.map { root + it }
    }
}

/**
 * Scale degree names (Roman numeral analysis)
 */
enum class ScaleDegree(val degree: Int) {
    TONIC(0),      // I - home, stable
    SUPERTONIC(1), // II
    MEDIANT(2),    // III
    SUBDOMINANT(3),// IV - moves away from tonic
    DOMINANT(4),   // V - strong pull to tonic
    SUBMEDIANT(5), // VI
    SUBTONIC(6);   // VII (in natural minor)

    fun getNote(scale: List<Int>): Int {
        return scale[degree % scale.size]
    }
}
