package com.dubtechno.generator.generative

import kotlin.random.Random

/**
 * Markov Chain for melodic sequence generation
 *
 * A Markov chain models the probability of transitioning from one state to another.
 * For music, this creates melodies that have coherence (notes relate to previous notes)
 * while still having variation.
 *
 * @param order The number of previous states to consider (1 = first-order Markov chain)
 */
class MarkovChain(private val order: Int = 1) {

    // Maps from sequence of notes to possible next notes and their frequencies
    private val transitions = mutableMapOf<List<Int>, MutableMap<Int, Int>>()

    // Custom transition weights (overrides learned probabilities)
    private val customWeights = mutableMapOf<Pair<Int, Int>, Float>()

    init {
        require(order > 0) { "Order must be positive" }
    }

    /**
     * Train the Markov chain on a sequence of notes
     */
    fun train(sequence: List<Int>) {
        require(sequence.size > order) { "Sequence must be longer than order" }

        for (i in 0..(sequence.size - order - 1)) {
            val state = sequence.subList(i, i + order)
            val nextNote = sequence[i + order]

            val nextStates = transitions.getOrPut(state) { mutableMapOf() }
            nextStates[nextNote] = nextStates.getOrDefault(nextNote, 0) + 1
        }
    }

    /**
     * Generate a new sequence
     *
     * @param length Desired length of generated sequence
     * @param seed Starting note(s). If null, picks random from trained data
     * @param randomSeed Random seed for reproducibility
     */
    fun generate(length: Int, seed: List<Int>? = null, randomSeed: Long? = null): List<Int> {
        val random = randomSeed?.let { Random(it) } ?: Random.Default

        if (transitions.isEmpty()) {
            throw IllegalStateException("Markov chain not trained. Call train() first.")
        }

        val result = mutableListOf<Int>()

        // Initialize with seed or random state
        val initialState = seed?.takeLast(order) ?: transitions.keys.random(random)
        result.addAll(initialState)

        // Generate remaining notes
        while (result.size < length) {
            val currentState = result.takeLast(order)
            val nextNote = selectNextNote(currentState, random)

            if (nextNote != null) {
                result.add(nextNote)
            } else {
                // No transitions found, pick random from any trained transitions
                val randomNext = transitions.values.flatMap { it.keys }.randomOrNull(random)
                if (randomNext != null) {
                    result.add(randomNext)
                } else {
                    break // Can't continue
                }
            }
        }

        return result.take(length)
    }

    /**
     * Select next note based on transition probabilities
     */
    private fun selectNextNote(state: List<Int>, random: Random): Int? {
        val possibleNextStates = transitions[state] ?: return null

        // Apply custom weights if any
        val weightedStates = possibleNextStates.mapValues { (note, frequency) ->
            val previousNote = state.lastOrNull() ?: return@mapValues frequency.toFloat()
            val customWeight = customWeights[Pair(previousNote, note)]
            if (customWeight != null) {
                frequency * customWeight
            } else {
                frequency.toFloat()
            }
        }

        // Weighted random selection
        val totalWeight = weightedStates.values.sum()
        var randomValue = random.nextFloat() * totalWeight

        for ((note, weight) in weightedStates) {
            randomValue -= weight
            if (randomValue <= 0) {
                return note
            }
        }

        return possibleNextStates.keys.first()
    }

    /**
     * Set custom transition weights
     * Weight of 2.0 makes transition twice as likely, 0.5 makes it half as likely
     */
    fun setTransitionWeights(weights: Map<Pair<Int, Int>, Float>) {
        customWeights.clear()
        customWeights.putAll(weights)
    }

    /**
     * Clear all trained data
     */
    fun clear() {
        transitions.clear()
        customWeights.clear()
    }

    /**
     * Get probability of transitioning from one note to another
     */
    fun getTransitionProbability(from: Int, to: Int): Float {
        val state = listOf(from)
        val nextStates = transitions[state] ?: return 0f

        val frequency = nextStates[to] ?: return 0f
        val total = nextStates.values.sum()

        return frequency.toFloat() / total
    }
}

/**
 * Pre-configured Markov chains for dub techno
 */
object DubTechnoMarkov {

    /**
     * Create a melody-focused Markov chain for dub techno
     *
     * Characteristics:
     * - Favors stepwise motion (2nds, 3rds)
     * - High probability of staying on same note (hypnotic)
     * - Avoids large jumps
     * - Periodically resolves to root
     */
    fun createMelodyChain(scale: List<Int>, hypnotic: Boolean = true): MarkovChain {
        val chain = MarkovChain(order = 1)

        // Create training sequence with desired characteristics
        val trainingSequence = buildDubMelodyTrainingSequence(scale, hypnotic)

        chain.train(trainingSequence)

        // Set custom weights to enforce dub techno style
        val weights = mutableMapOf<Pair<Int, Int>, Float>()

        for (i in scale.indices) {
            for (j in scale.indices) {
                val from = scale[i]
                val to = scale[j]
                val interval = Math.abs(i - j)

                val weight = when {
                    interval == 0 -> if (hypnotic) 3.0f else 2.0f // Same note (common)
                    interval == 1 -> 2.5f // Stepwise (very common)
                    interval == 2 -> 1.5f // Skip (occasional)
                    interval == 3 || interval == 4 -> 0.8f // Larger skip (rare)
                    else -> 0.3f // Large jump (very rare)
                }

                // Extra weight for resolving to root
                val finalWeight = if (to == scale[0]) weight * 1.5f else weight

                weights[Pair(from, to)] = finalWeight
            }
        }

        chain.setTransitionWeights(weights)

        return chain
    }

    /**
     * Create training sequence with dub techno characteristics
     */
    private fun buildDubMelodyTrainingSequence(scale: List<Int>, hypnotic: Boolean): List<Int> {
        val sequence = mutableListOf<Int>()
        val root = scale[0]

        // Repeat short motifs (typical of dub techno)
        repeat(10) {
            // Motif 1: Root held
            repeat(if (hypnotic) 8 else 4) { sequence.add(root) }

            // Motif 2: Root - 2nd - root
            sequence.add(root)
            sequence.add(scale[1])
            sequence.add(root)

            // Motif 3: Root - 3rd - 2nd - root
            sequence.add(root)
            sequence.add(scale[2])
            sequence.add(scale[1])
            sequence.add(root)

            // Motif 4: Root - 5th - root
            if (scale.size > 4) {
                sequence.add(root)
                sequence.add(scale[4])
                sequence.add(root)
            }
        }

        return sequence
    }

    /**
     * Create a bass-focused Markov chain
     *
     * Even more static than melody, mostly root with occasional movement
     */
    fun createBassChain(scale: List<Int>): MarkovChain {
        val chain = MarkovChain(order = 1)

        val root = scale[0]
        val fifth = if (scale.size > 4) scale[4] else scale[0]
        val fourth = if (scale.size > 3) scale[3] else scale[0]

        // Training sequence: mostly root
        val sequence = mutableListOf<Int>()
        repeat(50) {
            sequence.add(root)
        }
        repeat(5) {
            sequence.add(fifth)
        }
        repeat(5) {
            sequence.add(fourth)
        }
        repeat(10) {
            sequence.add(root)
        }

        chain.train(sequence)

        return chain
    }

    /**
     * Create a more experimental/chaotic Markov chain
     */
    fun createExperimentalChain(scale: List<Int>): MarkovChain {
        val chain = MarkovChain(order = 1)

        // More random training sequence
        val sequence = mutableListOf<Int>()
        val random = Random.Default

        repeat(100) {
            sequence.add(scale.random(random))
        }

        chain.train(sequence)

        // More balanced weights (less bias toward stepwise)
        val weights = mutableMapOf<Pair<Int, Int>, Float>()
        for (from in scale) {
            for (to in scale) {
                weights[Pair(from, to)] = 1.0f // Equal probability
            }
        }

        chain.setTransitionWeights(weights)

        return chain
    }
}
