package com.dubtechno.generator

import com.dubtechno.generator.generative.MusicTheory
import com.dubtechno.generator.model.ChordType
import com.dubtechno.generator.model.ScaleType
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for music theory utilities
 */
class MusicTheoryTest {

    @Test
    fun `test natural minor scale generation`() {
        val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR) // C minor

        val expected = listOf(60, 62, 63, 65, 67, 68, 70) // C, D, Eb, F, G, Ab, Bb
        assertEquals(expected, scale)
    }

    @Test
    fun `test dorian scale generation`() {
        val scale = MusicTheory.getScaleNotes(60, ScaleType.DORIAN)

        val expected = listOf(60, 62, 63, 65, 67, 69, 70) // C, D, Eb, F, G, A, Bb
        assertEquals(expected, scale)
    }

    @Test
    fun `test minor pentatonic scale`() {
        val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR_PENTATONIC)

        val expected = listOf(60, 63, 65, 67, 70) // C, Eb, F, G, Bb
        assertEquals(expected, scale)
        assertEquals(5, scale.size) // Pentatonic = 5 notes
    }

    @Test
    fun `test note to frequency conversion`() {
        // A4 = MIDI 69 = 440 Hz
        val freq = MusicTheory.noteToFrequency(69)
        assertEquals(440f, freq, 0.1f)

        // C4 = MIDI 60 = ~261.63 Hz
        val freqC = MusicTheory.noteToFrequency(60)
        assertEquals(261.63f, freqC, 0.1f)

        // Octave up should double frequency
        val freqC5 = MusicTheory.noteToFrequency(72)
        assertEquals(freqC * 2, freqC5, 0.1f)
    }

    @Test
    fun `test chord generation`() {
        val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR)

        val minorChord = MusicTheory.getChordNotes(60, ChordType.MINOR, scale)
        assertEquals(3, minorChord.size)
        assertEquals(60, minorChord[0]) // Root

        val minor7Chord = MusicTheory.getChordNotes(60, ChordType.MINOR7, scale)
        assertEquals(4, minor7Chord.size)

        val powerChord = MusicTheory.getChordNotes(60, ChordType.POWER, scale)
        assertEquals(2, powerChord.size)
    }

    @Test
    fun `test interval names`() {
        assertEquals("Unison/Octave", MusicTheory.getIntervalName(0))
        assertEquals("Minor 2nd", MusicTheory.getIntervalName(1))
        assertEquals("Major 2nd", MusicTheory.getIntervalName(2))
        assertEquals("Minor 3rd", MusicTheory.getIntervalName(3))
        assertEquals("Perfect 5th", MusicTheory.getIntervalName(7))
        assertEquals("Unison/Octave", MusicTheory.getIntervalName(12))
    }

    @Test
    fun `test quantize to scale`() {
        val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR)

        // C# (61) should quantize to C (60) or D (62)
        val quantized = MusicTheory.quantizeToScale(61, scale)
        assertTrue(quantized == 60 || quantized == 62)

        // Notes already in scale should stay the same
        assertEquals(60, MusicTheory.quantizeToScale(60, scale))
        assertEquals(63, MusicTheory.quantizeToScale(63, scale))
    }

    @Test
    fun `test interval calculation`() {
        assertEquals(0, MusicTheory.getInterval(60, 60)) // Unison
        assertEquals(12, MusicTheory.getInterval(60, 72)) // Octave
        assertEquals(7, MusicTheory.getInterval(60, 67)) // Perfect 5th
    }

    @Test
    fun `test consonance detection`() {
        assertTrue(MusicTheory.isConsonant(0))  // Unison
        assertTrue(MusicTheory.isConsonant(3))  // Minor 3rd
        assertTrue(MusicTheory.isConsonant(7))  // Perfect 5th
        assertTrue(MusicTheory.isConsonant(12)) // Octave

        assertFalse(MusicTheory.isConsonant(1))  // Minor 2nd (dissonant)
        assertFalse(MusicTheory.isConsonant(6))  // Tritone (dissonant)
    }

    @Test
    fun `test stepwise probability favors small intervals`() {
        val prob0 = MusicTheory.getStepwiseProbability(0) // Same note
        val prob1 = MusicTheory.getStepwiseProbability(1) // Semitone
        val prob2 = MusicTheory.getStepwiseProbability(2) // Whole tone
        val prob7 = MusicTheory.getStepwiseProbability(7) // Perfect 5th
        val prob12 = MusicTheory.getStepwiseProbability(12) // Octave

        // Stepwise motion should have higher probability
        assertTrue(prob1 > prob7)
        assertTrue(prob2 > prob12)
    }

    @Test
    fun `test scale with multiple octaves`() {
        val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR, octaves = 2)

        // Should have 14 notes (7 notes × 2 octaves)
        assertEquals(14, scale.size)

        // First octave
        assertEquals(60, scale[0])  // C
        assertEquals(70, scale[6])  // Bb

        // Second octave
        assertEquals(72, scale[7])  // C (octave up)
        assertEquals(82, scale[13]) // Bb (octave up)
    }
}
