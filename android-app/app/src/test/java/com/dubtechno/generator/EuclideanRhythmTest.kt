package com.dubtechno.generator

import com.dubtechno.generator.generative.EuclideanRhythm
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for Euclidean rhythm algorithm
 */
class EuclideanRhythmTest {

    @Test
    fun `test E(4,16) generates four-on-floor`() {
        val rhythm = EuclideanRhythm.generate(4, 16)

        // Should have exactly 4 hits
        assertEquals(4, rhythm.count { it })

        // Should be evenly distributed (every 4 steps)
        assertTrue(rhythm[0])  // Beat 1
        assertTrue(rhythm[4])  // Beat 2
        assertTrue(rhythm[8])  // Beat 3
        assertTrue(rhythm[12]) // Beat 4
    }

    @Test
    fun `test E(5,8) generates tresillo`() {
        val rhythm = EuclideanRhythm.generate(5, 8)

        // Should have exactly 5 hits
        assertEquals(5, rhythm.count { it })

        // Classic Cuban tresillo pattern
        val expected = booleanArrayOf(true, false, true, false, true, false, true, true)
        assertArrayEquals(expected, rhythm)
    }

    @Test
    fun `test E(3,8) generates basic tresillo`() {
        val rhythm = EuclideanRhythm.generate(3, 8)

        // Should have exactly 3 hits
        assertEquals(3, rhythm.count { it })

        // Evenly distributed
        assertTrue(rhythm[0])
        assertTrue(rhythm[3])
        assertTrue(rhythm[6])
    }

    @Test
    fun `test E(0,16) generates all rests`() {
        val rhythm = EuclideanRhythm.generate(0, 16)

        // Should have no hits
        assertEquals(0, rhythm.count { it })
    }

    @Test
    fun `test E(16,16) generates all hits`() {
        val rhythm = EuclideanRhythm.generate(16, 16)

        // Should have all hits
        assertEquals(16, rhythm.count { it })
    }

    @Test
    fun `test rotation shifts pattern`() {
        val original = EuclideanRhythm.generate(4, 16, rotation = 0)
        val rotated = EuclideanRhythm.generate(4, 16, rotation = 1)

        // Same number of hits
        assertEquals(original.count { it }, rotated.count { it })

        // But different positions
        assertFalse(original.contentEquals(rotated))
    }

    @Test
    fun `test pattern generation with note`() {
        val pattern = EuclideanRhythm.generatePattern(
            hits = 4,
            steps = 16,
            note = 60, // Middle C
            velocity = 0.8f
        )

        assertEquals(16, pattern.length)
        assertEquals(4, pattern.countHits())

        // Check notes have correct properties
        val firstNote = pattern.notes.first { it != null }
        assertNotNull(firstNote)
        assertEquals(60, firstNote!!.midiNote)
        assertEquals(0.8f, firstNote.velocity, 0.01f)
    }

    @Test
    fun `test preset patterns have correct characteristics`() {
        val kick = EuclideanRhythm.DubPresets.kickFourOnFloor()
        assertEquals(16, kick.length)
        assertTrue(kick.countHits() > 0)

        val snare = EuclideanRhythm.DubPresets.snareBackbeat()
        assertEquals(16, snare.length)

        val hiHat = EuclideanRhythm.DubPresets.hiHatModerate()
        assertEquals(16, hiHat.length)
    }

    @Test
    fun `test humanized pattern has velocity variations`() {
        val pattern = EuclideanRhythm.generateHumanized(
            hits = 4,
            steps = 16,
            note = 60,
            baseVelocity = 0.7f,
            velocityVariation = 0.2f,
            seed = 12345L
        )

        val notes = pattern.notes.filterNotNull()
        val velocities = notes.map { it.velocity }.toSet()

        // Should have different velocities (humanized)
        assertTrue(velocities.size > 1)

        // All velocities should be in valid range
        velocities.forEach { velocity ->
            assertTrue(velocity in 0.1f..1.0f)
        }
    }
}
