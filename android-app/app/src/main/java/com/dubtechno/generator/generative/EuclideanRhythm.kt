package com.dubtechno.generator.generative

import com.dubtechno.generator.model.Note
import com.dubtechno.generator.model.Pattern

/**
 * Euclidean rhythm generator using Björklund's algorithm
 *
 * Euclidean rhythms distribute k hits over n steps as evenly as possible.
 * This creates musically interesting patterns found in traditional music worldwide.
 *
 * Examples:
 * - E(4, 16) = [X . . . X . . . X . . . X . . .] - four-on-floor kick
 * - E(5, 8) = [X . X . X . X X] - common Cuban tresillo
 * - E(3, 8) = [X . . X . . X .] - basic tresillo
 */
object EuclideanRhythm {

    /**
     * Generate Euclidean rhythm using Björklund's algorithm
     *
     * @param hits Number of hits/pulses (k)
     * @param steps Total number of steps (n)
     * @param rotation Rotate pattern by this many steps
     * @return BooleanArray where true = hit, false = rest
     */
    fun generate(hits: Int, steps: Int, rotation: Int = 0): BooleanArray {
        require(hits >= 0) { "Hits must be non-negative" }
        require(steps > 0) { "Steps must be positive" }
        require(hits <= steps) { "Hits cannot exceed steps" }

        if (hits == 0) {
            return BooleanArray(steps) { false }
        }

        if (hits == steps) {
            return BooleanArray(steps) { true }
        }

        // Björklund's algorithm
        val pattern = bjorklund(hits, steps)

        // Apply rotation
        val rotated = if (rotation != 0) {
            val rot = rotation % steps
            pattern.takeLast(rot).toTypedArray() + pattern.dropLast(rot).toTypedArray()
        } else {
            pattern
        }

        return rotated.map { it }.toBooleanArray()
    }

    /**
     * Björklund's algorithm implementation
     */
    private fun bjorklund(hits: Int, steps: Int): Array<Boolean> {
        // Build two groups: hits (true) and rests (false)
        val groups = mutableListOf<MutableList<Boolean>>()

        // Initialize with individual hits and rests
        repeat(hits) {
            groups.add(mutableListOf(true))
        }
        repeat(steps - hits) {
            groups.add(mutableListOf(false))
        }

        // Iteratively combine groups
        var numGroups = groups.size
        while (numGroups > 1) {
            val groupSize1 = groups.take(numGroups / 2).size
            val groupSize2 = numGroups - groupSize1

            if (groupSize2 <= 1) break

            // Combine first half with second half
            for (i in 0 until minOf(groupSize1, groupSize2)) {
                groups[i].addAll(groups[groupSize1 + i])
            }

            // Remove combined groups from the end
            repeat(minOf(groupSize1, groupSize2)) {
                groups.removeAt(groups.size - 1)
            }

            numGroups = groups.size
        }

        // Flatten to single array
        return groups.flatten().toTypedArray()
    }

    /**
     * Generate Pattern from Euclidean rhythm
     */
    fun generatePattern(
        hits: Int,
        steps: Int,
        note: Int,
        velocity: Float = 0.7f,
        rotation: Int = 0
    ): Pattern {
        val rhythm = generate(hits, steps, rotation)
        return Pattern.fromRhythm(rhythm, note, velocity)
    }

    /**
     * Dub techno preset patterns
     */
    object DubPresets {

        /** Standard four-on-floor kick pattern */
        fun kickFourOnFloor(velocity: Float = 0.8f): Pattern {
            return generatePattern(4, 16, 36, velocity) // E(4, 16)
        }

        /** Half-time kick (typical dub techno) */
        fun kickHalfTime(velocity: Float = 0.8f): Pattern {
            return generatePattern(2, 16, 36, velocity) // E(2, 16) - on beats 1 and 3
        }

        /** Syncopated kick with slight complexity */
        fun kickSyncopated(velocity: Float = 0.8f): Pattern {
            return generatePattern(5, 16, 36, velocity, rotation = 2) // E(5, 16)
        }

        /** Backbeat snare (2 and 4) */
        fun snareBackbeat(velocity: Float = 0.6f): Pattern {
            return generatePattern(2, 16, 38, velocity, rotation = 4) // E(2, 16) shifted
        }

        /** Sparse snare */
        fun snareSparse(velocity: Float = 0.6f): Pattern {
            return generatePattern(3, 16, 38, velocity, rotation = 6)
        }

        /** Moderate hi-hat pattern */
        fun hiHatModerate(velocity: Float = 0.5f): Pattern {
            return generatePattern(7, 16, 42, velocity) // E(7, 16)
        }

        /** Dense hi-hat pattern */
        fun hiHatDense(velocity: Float = 0.5f): Pattern {
            return generatePattern(11, 16, 42, velocity) // E(11, 16)
        }

        /** Sparse hi-hat */
        fun hiHatSparse(velocity: Float = 0.5f): Pattern {
            return generatePattern(5, 16, 42, velocity, rotation = 1)
        }

        /** Conga/percussion pattern */
        fun percussion(velocity: Float = 0.4f): Pattern {
            return generatePattern(5, 8, 45, velocity) // E(5, 8) - classic tresillo
        }

        /** Shaker pattern (very dense) */
        fun shaker(velocity: Float = 0.3f): Pattern {
            return generatePattern(13, 16, 47, velocity)
        }
    }

    /**
     * Generate Euclidean rhythm with probabilities
     * Each hit has a probability of actually triggering
     */
    fun generateWithProbability(
        hits: Int,
        steps: Int,
        note: Int,
        velocity: Float = 0.7f,
        probability: Float = 1.0f,
        rotation: Int = 0
    ): Pattern {
        val rhythm = generate(hits, steps, rotation)
        val notes = rhythm.map { hit ->
            if (hit) Note(note, velocity, 0.25f, probability) else null
        }
        return Pattern(notes, steps)
    }

    /**
     * Generate humanized Euclidean pattern with velocity variations
     */
    fun generateHumanized(
        hits: Int,
        steps: Int,
        note: Int,
        baseVelocity: Float = 0.7f,
        velocityVariation: Float = 0.2f,
        rotation: Int = 0,
        seed: Long? = null
    ): Pattern {
        val rhythm = generate(hits, steps, rotation)
        val random = seed?.let { java.util.Random(it) } ?: java.util.Random()

        val notes = rhythm.map { hit ->
            if (hit) {
                val variation = (random.nextFloat() - 0.5f) * velocityVariation
                val velocity = (baseVelocity + variation).coerceIn(0.1f, 1.0f)
                Note(note, velocity, 0.25f)
            } else {
                null
            }
        }

        return Pattern(notes, steps, seed)
    }
}
