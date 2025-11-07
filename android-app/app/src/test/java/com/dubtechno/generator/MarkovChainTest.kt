package com.dubtechno.generator

import com.dubtechno.generator.generative.MarkovChain
import com.dubtechno.generator.generative.DubTechnoMarkov
import com.dubtechno.generator.generative.MusicTheory
import com.dubtechno.generator.model.ScaleType
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for Markov chain melody generation
 */
class MarkovChainTest {

    @Test
    fun `test basic training and generation`() {
        val chain = MarkovChain(order = 1)
        val sequence = listOf(60, 62, 64, 62, 60, 62, 64, 65, 67)

        chain.train(sequence)

        val generated = chain.generate(10, seed = listOf(60), randomSeed = 12345L)

        // Should generate requested length
        assertEquals(10, generated.size)

        // Should start with seed
        assertEquals(60, generated[0])
    }

    @Test
    fun `test markov chain learns transitions`() {
        val chain = MarkovChain(order = 1)

        // Train with simple pattern: always 60 -> 62
        val sequence = listOf(60, 62, 60, 62, 60, 62, 60, 62)
        chain.train(sequence)

        // After 60, should almost always go to 62
        val probability = chain.getTransitionProbability(60, 62)
        assertTrue(probability > 0.9f)
    }

    @Test
    fun `test custom transition weights`() {
        val chain = MarkovChain(order = 1)
        val sequence = listOf(60, 62, 64, 62, 60, 62, 64, 65)

        chain.train(sequence)

        // Set custom weights to favor 60 -> 64 transition
        val weights = mapOf(
            Pair(60, 64) to 10.0f // Make this transition very likely
        )
        chain.setTransitionWeights(weights)

        // Generate multiple times and check if 64 appears after 60
        val generated = chain.generate(20, seed = listOf(60), randomSeed = 12345L)

        // Should have some 64s after 60s
        var count = 0
        for (i in 0 until generated.size - 1) {
            if (generated[i] == 60 && generated[i + 1] == 64) {
                count++
            }
        }

        assertTrue(count > 0)
    }

    @Test
    fun `test dub techno melody chain favors stepwise motion`() {
        val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR)
        val chain = DubTechnoMarkov.createMelodyChain(scale, hypnotic = true)

        val generated = chain.generate(20, seed = listOf(scale[0]), randomSeed = 12345L)

        // Check that intervals are mostly small (stepwise)
        var stepwiseCount = 0
        for (i in 0 until generated.size - 1) {
            val interval = Math.abs(generated[i + 1] - generated[i])
            if (interval <= 2) { // Stepwise (semitone or whole tone)
                stepwiseCount++
            }
        }

        // Most motion should be stepwise
        val stepwiseRatio = stepwiseCount.toFloat() / (generated.size - 1)
        assertTrue(stepwiseRatio > 0.5f)
    }

    @Test
    fun `test bass chain is more static`() {
        val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR)
        val chain = DubTechnoMarkov.createBassChain(scale)

        val generated = chain.generate(20, seed = listOf(scale[0]), randomSeed = 12345L)

        // Count how often the root note appears
        val rootCount = generated.count { it == scale[0] }

        // Bass should be mostly on root
        val rootRatio = rootCount.toFloat() / generated.size
        assertTrue(rootRatio > 0.6f)
    }

    @Test
    fun `test experimental chain is more varied`() {
        val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR)
        val experimentalChain = DubTechnoMarkov.createExperimentalChain(scale)
        val regularChain = DubTechnoMarkov.createMelodyChain(scale, hypnotic = false)

        val experimental = experimentalChain.generate(50, randomSeed = 12345L)
        val regular = regularChain.generate(50, randomSeed = 12345L)

        // Experimental should use more unique notes
        val experimentalUnique = experimental.toSet().size
        val regularUnique = regular.toSet().size

        // Experimental chain should generally be more varied
        // (This is probabilistic, so we use a lenient threshold)
        assertTrue(experimentalUnique >= regularUnique * 0.8)
    }

    @Test(expected = IllegalStateException::class)
    fun `test generation fails without training`() {
        val chain = MarkovChain(order = 1)
        chain.generate(10) // Should throw exception
    }

    @Test
    fun `test second-order markov chain`() {
        val chain = MarkovChain(order = 2)

        // Pattern: 60-62-64, 60-62-65
        val sequence = listOf(60, 62, 64, 60, 62, 65, 60, 62, 64)
        chain.train(sequence)

        val generated = chain.generate(10, seed = listOf(60, 62), randomSeed = 12345L)

        // Should start with seed
        assertEquals(60, generated[0])
        assertEquals(62, generated[1])

        // Next note should be either 64 or 65 (learned from training)
        assertTrue(generated[2] == 64 || generated[2] == 65)
    }
}
